package actors.animations

import kotlinx.serialization.Serializable
import ui.input.Keyboard
import util.XY

@Serializable
class Step(
    val dir: XY
) : Animation(Keyboard.REPEAT_MS) {

    override fun shadowOffsetX() = (-1f + progress()) * dir.x
    override fun shadowOffsetY() = (-1f + progress()) * dir.y
    override fun offsetX() = shadowOffsetX()
    override fun offsetY(): Float {
        val p = progress()
        return shadowOffsetY() - (if (p < 0.5f) p * 0.35f else (1f - p) * 0.35f)
    }

}
