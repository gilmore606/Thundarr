package actors.animations

import kotlinx.serialization.Serializable
import util.XY
import util.log
import java.lang.Integer.min

@Serializable
class Whack(
    val dir: XY,
    val reach: Float = 0.8f,
    val whackDuration: Long = 300,
) : Animation(whackDuration) {

    var ox = 0f
    var oy = 0f

    override fun doOnRender(delta: Float) {
        var p = progress()
        if (p <= 0.2f) {
            p /= 0.2f
            ox = dir.x.toFloat() * p * reach
            oy = dir.y.toFloat() * p * reach
        } else if (p <= 0.5f) {
            p = (p - 0.2f) / 0.6f + 0.2f
            ox = dir.x.toFloat() * (1f - p) * reach
            oy = dir.y.toFloat() * (1f - p) * reach
        } else {
            p  = (p - 0.5f)
            ox = dir.x.toFloat() * (0.5f - p) * reach
            oy = dir.y.toFloat() * (0.5f - p) * reach
        }
    }
    override fun offsetX() = ox
    override fun offsetY() = oy
    override fun shadowOffsetX() = ox
}
