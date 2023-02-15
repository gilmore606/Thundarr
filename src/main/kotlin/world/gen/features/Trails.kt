package world.gen.features

import kotlinx.serialization.Serializable
import util.Dice
import util.Rect
import util.XY
import util.getBezier
import world.level.CHUNK_SIZE

@Serializable
class Trails(
    val exits: MutableList<TrailExit>
) : ChunkFeature(
    2, Stage.TERRAIN
) {

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
                val center = TrailExit(pos = XY(centerX, centerY), control = XY(centerX, centerY), edge = XY(0,0))
                exits.forEach { exit ->
                    drawTrail(exit, center)
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
