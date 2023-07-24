package actors.animations

import ui.input.Keyboard
import util.XY
import java.lang.Float.max
import java.lang.Float.min

class Leap(source: XY, dest: XY): Animation(Keyboard.REPEAT_MS * 2) {

    val xdist = (dest.x - source.x)
    val ydist = (dest.y - source.y)
    val height = min(4f, max(1.5f, max(xdist.toFloat(), ydist.toFloat()) / 2f))

    override fun shadowOffsetX() = (-1f + progress()) * xdist
    override fun shadowOffsetY() = (-1f + progress()) * ydist
    override fun offsetX() = shadowOffsetX()
    override fun offsetY(): Float {
        val p = progress()
        return shadowOffsetY() - (if (p < 0.5f) p * height else (1f - p) * height)
    }

}
