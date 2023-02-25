package world.gen.features

import kotlinx.serialization.Serializable
import util.*
import world.ChunkScratch
import world.gen.biomes.Ruins
import world.gen.biomes.Suburb
import world.level.CHUNK_SIZE

@Serializable
class Trails(
    val exits: MutableList<TrailExit>
) : ChunkFeature(
    2, Stage.TERRAIN
) {
    companion object {
        fun canBuildOn(meta: ChunkScratch) = meta.biome !in listOf(Ruins, Suburb)
    }

    @Serializable
    class TrailExit(
        var pos: XY,
        var edge: XY,
        var control: XY
    )

    fun addExit(exit: TrailExit) {
        exits.add(exit)
    }

    override fun doDig() {
        carto.trailHead?.also { centerPos ->
            val center = TrailExit(pos = centerPos, control = centerPos, edge = NO_DIRECTION)
            exits.forEach { exit ->
                drawTrail(exit, center)
            }
        } ?: run {
            when (exits.size) {
                1 -> {
                    val start = exits[0]
                    val endPos = XY(Dice.range(CHUNK_SIZE / 4, (CHUNK_SIZE / 4 * 3)), Dice.range(CHUNK_SIZE / 4, (CHUNK_SIZE / 4 * 3)))
                    val end = TrailExit(pos = endPos, control = endPos, edge = XY(0,0))
                    drawTrail(start, end)
                }
                2 -> {
                    drawTrail(exits[0], exits[1])
                }
                else -> {
                    val variance = ((CHUNK_SIZE / 2) * 0.2f).toInt()
                    val centerX = (CHUNK_SIZE / 2) + Dice.zeroTil(variance) - (variance / 2)
                    val centerY = (CHUNK_SIZE / 2) + Dice.zeroTil(variance) - (variance / 2)
                    val center = TrailExit(pos = XY(centerX, centerY), control = XY(centerX, centerY), edge = NO_DIRECTION)
                    exits.forEach { exit ->
                        drawTrail(exit, center)
                    }
                }
            }
        }
    }

    private fun drawTrail(start: TrailExit, end: TrailExit) {
        var t = 0f
        val step = 0.02f
        while (t < 1f) {
            val p = getBezier(t, start.pos.toXYf(), start.control.toXYf(), end.control.toXYf(), end.pos.toXYf())
            val terrain = biomeAt(x0 + p.x.toInt(), y0 + p.y.toInt()).trailTerrain(x0 + p.x.toInt(), y0 + p.y.toInt())
            carveTrailChunk(
                Rect((x0 + p.x).toInt(), (y0 + p.y).toInt(),
                (x0 + p.x + 1).toInt(), (y0 + p.y + 1).toInt()), terrain, false)
            t += step
        }
    }
}
