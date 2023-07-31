package actors.animations

import kotlinx.serialization.Serializable
import ui.input.Keyboard
import util.XY

@Serializable
class Slide(
    val dir: XY
) : Animation(Keyboard.REPEAT_MS) {
    override fun shadowOffsetX() = (-1f + progress()) * dir.x
    override fun shadowOffsetY() = (-1f + progress()) * dir.y
    override fun offsetX() = (-1f + progress()) * dir.x
    override fun offsetY() = (-1f + progress()) * dir.y

}
