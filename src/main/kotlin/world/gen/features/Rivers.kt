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
        fuzzTerrain(Terrain.Type.GENERIC_WATER, riverBlur * 0.4f)
        addRiverBanks()
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
                if (getTerrain(x, y) != Terrain.Type.GENERIC_WATER && !flagsAt(x,y).contains(WorldCarto.CellFlag.RIVERBANK) &&
                    !flagsAt(x,y).contains(WorldCarto.CellFlag.TRAIL)) {
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
        val startWidth = if (start.edge in meta.coasts()) start.width * 2f + 3f else start.width.toFloat()
        val endWidth = if (end.edge in meta.coasts()) end.width * 2f + 3f else end.width.toFloat()
        var t = 0f
        var width = startWidth
        val step = 0.02f
        val widthStep = (endWidth - startWidth) * step
        while (t < 1f) {
            val p = getBezier(t, start.pos.toXYf(), start.control.toXYf(), end.control.toXYf(), end.pos.toXYf())
            carveRiverChunk(
                Rect((x0 + p.x - width/2).toInt(), (y0 + p.y - width/2).toInt(),
                (x0 + p.x + width/2).toInt(), (y0 + p.y + width/2).toInt()), (width >= 3f))
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
    }

    private fun carveRiverChunk(room: Rect, skipCorners: Boolean) {
        for (x in room.x0..room.x1) {
            for (y in room.y0..room.y1) {
                if (x >= x0 && y >= y0 && x <= x1 && y <= y1) {
                    if (!skipCorners || !((x == x0 || x == x1) && (y == y0 || y == y1))) {
                        setTerrain(x, y, Terrain.Type.GENERIC_WATER)
                        flagsAt(x,y).add(WorldCarto.CellFlag.RIVER)
                    }
                }
            }
        }
    }
}
