package world.gen.features

import audio.Speaker
import kotlinx.serialization.Serializable
import util.*
import world.gen.NoisePatches
import world.gen.cartos.WorldCarto
import world.level.CHUNK_SIZE
import world.terrains.Terrain

@Serializable
class Rivers(
    val exits: MutableList<RiverExit>,
) : ChunkFeature(
    4, Stage.TERRAIN
) {

    private val harborChance = 0.8f
    private val lonelyBridgeChance = 0.05f

    @Serializable
    class RiverExit(
        var pos: XY,
        var edge: XY,
        var width: Int = 4,
        var control: XY
    )

    var riverBlur: Float = 0.5f

    private val riverIslandPoints = ArrayList<XY>()

    fun addExit(exit: RiverExit) {
        exits.add(exit)
    }

    override fun trailDestinationChance() = 0.1f

    override fun doDig() {
        when (exits.size) {
            1 -> {
                val start = exits[0]
                val endPos = XY(Dice.range(CHUNK_SIZE / 4, (CHUNK_SIZE / 4 * 3)), Dice.range(CHUNK_SIZE / 4, (CHUNK_SIZE / 4 * 3)))
                val end = RiverExit(pos = endPos, control = endPos, width = 1, edge = XY(0,0))
                drawRiver(start, end)
            }
            2 -> {
                drawRiver(exits[0], exits[1])
            }
            else -> {
                val variance = ((CHUNK_SIZE / 2) * 0.2f).toInt()
                val centerX = (CHUNK_SIZE / 2) + Dice.zeroTil(variance) - (variance / 2)
                val centerY = (CHUNK_SIZE / 2) + Dice.zeroTil(variance) - (variance / 2)
                val centerWidth = exits.maxOf { it.width }
                val center = RiverExit(pos = XY(centerX, centerY), control = XY(centerX, centerY), width = centerWidth, edge = XY(0,0))
                exits.forEach { exit ->
                    drawRiver(exit, center)
                }
            }
        }

        if (riverIslandPoints.isNotEmpty() && Dice.chance(0.5f)) {
            riverIslandPoints.random().also { island ->
                printGrid(growBlob(Dice.range(3,6), Dice.range(3,6)), island.x, island.y, meta.biome.baseTerrain)
            }
        }

        // Fuzz water but don't overwrite bridge slots
        val adds = ArrayList<XY>()
        val density = riverBlur * 0.4f
        forEachTerrain(Terrain.Type.GENERIC_WATER) { x, y ->
            carto.neighborsAt(x,y,CARDINALS) { nx, ny, terrain ->
                if (terrain != Terrain.Type.GENERIC_WATER && !flagsAt(x,y).contains(WorldCarto.CellFlag.BRIDGE_SLOT)) {
                    if (Dice.chance(density)) adds.add(XY(nx,ny))
                }
            }
        }
        adds.forEach { setTerrain(it.x, it.y, Terrain.Type.GENERIC_WATER) }
        addRiverBanks()

        if (Dice.chance(lonelyBridgeChance)) {
            val terrain = if (Dice.flip()) Terrain.Type.TERRAIN_WOODFLOOR else Terrain.Type.TERRAIN_STONEFLOOR
            forEachCell { x,y ->
                if (flagsAt(x,y).contains(WorldCarto.CellFlag.BRIDGE_SLOT)) setTerrain(x, y, terrain)
            }
        }
    }

    private fun addRiverBanks() {
        forEachBiome { x, y, biome ->
            if (getTerrain(x,y) == Terrain.Type.GENERIC_WATER) {
                DIRECTIONS.from(x, y) { dx, dy, _ ->
                    if (boundsCheck(dx, dy) && getTerrain(dx, dy) != Terrain.Type.GENERIC_WATER) {
                        if (getTerrain(dx, dy) != Terrain.Type.TERRAIN_BEACH) {
                            flagsAt(dx,dy).add(WorldCarto.CellFlag.RIVERBANK)
                        }
                    }
                }
            }
        }
        repeat (3) {
            val adds = ArrayList<XY>()
            forEachBiome { x, y, biome ->
                if (getTerrain(x, y) != Terrain.Type.GENERIC_WATER && !flagsAt(x,y).contains(WorldCarto.CellFlag.RIVERBANK)) {
                    var n = 0
                    DIRECTIONS.from(x, y) { dx, dy, dir ->
                        if (boundsCheck(dx,dy) && flagsAt(dx,dy).contains(WorldCarto.CellFlag.RIVERBANK)) n++
                    }
                    val v = (NoisePatches.get("ruinMicro", x, y) * 3f).toInt()
                    if (n > 1 && Dice.chance(n * 0.15f + v * 0.4f)) adds.add(XY(x,y))
                }
            }
            adds.forEach { flagsAt(it.x, it.y).add(WorldCarto.CellFlag.RIVERBANK) }
        }
        forEachBiome { x,y,biome ->
            if (flagsAt(x,y).contains(WorldCarto.CellFlag.RIVERBANK)) {
                setTerrain(x,y,biome.riverBankTerrain(x,y))
            }
        }
    }

    private fun drawRiver(start: RiverExit, end: RiverExit) {
        val startMouth = start.edge in meta.coasts()
        val endMouth = end.edge in meta.coasts()
        val startWidth = if (startMouth) start.width * 2f + 3f else start.width.toFloat()
        val endWidth = if (endMouth) end.width * 2f + 3f else end.width.toFloat()
        var t = 0f
        var width = startWidth
        val step = 0.02f
        val widthStep = (endWidth - startWidth) * step
        val bridgeX = if (start.edge in listOf(EAST, WEST) && end.edge in listOf(EAST, WEST)) x0 + 15 + Dice.zeroTo(30) else -1
        val bridgeY = if (start.edge in listOf(NORTH, SOUTH) && end.edge in listOf(NORTH, SOUTH)) y0 + 15 + Dice.zeroTo(30) else -1
        val wideBridge = Dice.flip()
        while (t < 1f) {
            val p = getBezier(t, start.pos.toXYf(), start.control.toXYf(), end.control.toXYf(), end.pos.toXYf())
            carveRiverChunk(
                Rect((x0 + p.x - width/2).toInt(), (y0 + p.y - width/2).toInt(),
                (x0 + p.x + width/2).toInt(), (y0 + p.y + width/2).toInt()), (width >= 3f),
                bridgeX, bridgeY, wideBridge)
            if (t > 0.2f && t < 0.8f && width > 6 && Dice.chance(0.1f)) {
                riverIslandPoints.add(XY((x0 + p.x).toInt(), (y0 + p.y).toInt()))
            }
            chunk.setSound(x0 + p.x.toInt(), y0 + p.y.toInt(),
                if (width < 4) Speaker.PointAmbience(Speaker.Ambience.RIVER1, 30f, 1f)
                else if (width < 8) Speaker.PointAmbience(Speaker.Ambience.RIVER2, 40f, 1f)
                else Speaker.PointAmbience(Speaker.Ambience.RIVER3, 50f, 1f))
            t += step
            width += widthStep
        }
        if ((startMouth || endMouth) && Dice.chance(harborChance) && width > 3f) {
            val pos = if (startMouth) start.pos else end.pos
            val w = width.toInt()
            printGrid(
                growBlob(Dice.range(w, w * 3), Dice.range(w, w * 3)),
                x0 + pos.x - Dice.range(1, w * 2), y0 + pos.y - Dice.range(1, w * 2),
                Terrain.Type.GENERIC_WATER
            )
        }
    }

    private fun carveRiverChunk(room: Rect, skipCorners: Boolean,
                                bridgeX: Int, bridgeY: Int, wideBridge: Boolean) {
        for (x in room.x0..room.x1) {
            for (y in room.y0..room.y1) {
                if (x >= x0 && y >= y0 && x <= x1 && y <= y1) {
                    if (!skipCorners || !((x == x0 || x == x1) && (y == y0 || y == y1))) {
                        if (x != bridgeX && y != bridgeY && (!wideBridge || (x != bridgeX + 1 && y != bridgeY + 1))) {
                            carto.blockTrailAt(x,y)
                        } else {
                            flagsAt(x,y).add(WorldCarto.CellFlag.BRIDGE_SLOT)
                        }
                        setTerrain(x, y, Terrain.Type.GENERIC_WATER)
                        flagsAt(x,y).add(WorldCarto.CellFlag.RIVER)
                    }
                }
            }
        }
    }
}
