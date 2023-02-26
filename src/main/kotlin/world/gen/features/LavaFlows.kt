package world.gen.features

import audio.Speaker
import kotlinx.serialization.Serializable
import util.*
import world.gen.NoisePatches
import world.level.CHUNK_SIZE
import world.terrains.Terrain

@Serializable
class LavaFlows(
    val exits: MutableList<LavaExit>,
) : ChunkFeature(
    1, Stage.BUILD
) {

    @Serializable
    class LavaExit(
        var pos: XY,
        var edge: XY,
        var width: Int = 4
    )

    fun addExit(exit: LavaExit) {
        exits.add(exit)
    }

    override fun doDig() {
        var bridgeDir: XY? = null
        when (exits.size) {
            1 -> {
                val start = exits[0]
                val endPos = XY(Dice.range(CHUNK_SIZE / 4, (CHUNK_SIZE / 4 * 3)), Dice.range(CHUNK_SIZE / 4, (CHUNK_SIZE / 4 * 3)))
                val end = LavaExit(pos = endPos, edge = XY(0,0), width = 1)
                drawLava(start, end)
            }
            2 -> {
                val e1 = exits[0]
                val e2 = exits[1]
                drawLava(e1, e2)
                if (e1.width <= 6) {
                    if ((e1.edge == NORTH && e2.edge == SOUTH) || (e1.edge == SOUTH && e2.edge == NORTH)) {
                        bridgeDir = EAST
                    } else if ((e1.edge == EAST && e2.edge == WEST) || (e1.edge == WEST && e2.edge == EAST)) {
                        bridgeDir = NORTH
                    }
                }
            }
            else -> {
                val variance = ((CHUNK_SIZE / 2) * 0.2f).toInt()
                val centerX = (CHUNK_SIZE / 2) + Dice.zeroTil(variance) - (variance / 2)
                val centerY = (CHUNK_SIZE / 2) + Dice.zeroTil(variance) - (variance / 2)
                val center = LavaExit(pos = XY(centerX, centerY), edge = XY(0,0), width = exits[0].width + 1)
                exits.forEach { exit ->
                    drawLava(exit, center)
                }
            }
        }
        fuzzTerrain(Terrain.Type.TERRAIN_LAVA, 0.5f)
        bridgeDir?.also { addLavaBridge(it) }
        addLavaSound()
    }

    private fun drawLava(start: LavaExit, end: LavaExit) {
        var t = 0f
        val step = 0.02f
        var width = start.width.toFloat()
        val widthStep = (end.width.toFloat() - start.width.toFloat()) * step
        while (t < 1f) {
            val p = getBezier(t, start.pos.toXYf(), start.pos.toXYf(), end.pos.toXYf(), end.pos.toXYf())
            val px = x0 + p.x
            val py = y0 + p.y
            carveFlowBlob(Rect((px - width/2).toInt(), (py - width/2).toInt(),
                (px + width/2).toInt(), (py + width/2).toInt()), Terrain.Type.TERRAIN_LAVA, false)
            val edgeWidth = width + 2.5f + width * (NoisePatches.get("metaVariance",px.toInt(),py.toInt()).toFloat()) * 1.5f
            carveFlowBlob(Rect((px - edgeWidth / 2).toInt(), (py - edgeWidth / 2).toInt(),
                (px + edgeWidth/2).toInt(), (py + edgeWidth/2).toInt()),
                Terrain.Type.TERRAIN_ROCKS, true,
                Terrain.Type.TERRAIN_LAVA
            )
            t += step
            width += widthStep
        }
    }

    private fun addLavaBridge(dir: XY) {
        val cross = (CHUNK_SIZE / 4) + Dice.zeroTo(CHUNK_SIZE-30)
        for (i in 0 until CHUNK_SIZE) {
            val x = x0 + if (dir == NORTH) cross else i
            val y = y0 + if (dir == NORTH) i else cross
            if (getTerrain(x,y) == Terrain.Type.TERRAIN_LAVA) {
                setTerrain(x,y, Terrain.Type.TERRAIN_ROCKS)
            }
        }
    }

    private fun addLavaSound() {
        forEachTerrain(Terrain.Type.TERRAIN_LAVA) { x, y ->
            if (CARDINALS.hasOneWhere { boundsCheck(x+it.x, y+it.y) && getTerrain(x+it.x, y+it.y) != Terrain.Type.TERRAIN_LAVA }) {
                chunk.setSound(x, y, Speaker.PointAmbience(Speaker.Ambience.LAVA, 35f, 1f))
            }
        }
    }
}
