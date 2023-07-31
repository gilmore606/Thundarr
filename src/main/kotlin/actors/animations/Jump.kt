package actors.animations

import kotlinx.serialization.Serializable
import ui.input.Keyboard
import util.XY

@Serializable
class Jump(
    val dir: XY
) : Animation(Keyboard.REPEAT_MS) {

    val height = 0.7f
    override fun shadowOffsetX() = (-1f + progress()) * dir.x
    override fun shadowOffsetY() = (-1f + progress()) * dir.y
    override fun offsetX() = shadowOffsetX()
    override fun offsetY(): Float {
        val p = progress()
        return shadowOffsetY() - (if (p < 0.5f) p * height else (1f - p) * height)
    }

}
