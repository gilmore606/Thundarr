package actors.animations

import ui.input.Keyboard
import util.XY

class Slide(
    val dir: XY
) : Animation(Keyboard.REPEAT_MS) {

    override fun offsetX() = (-1f + progress()) * dir.x
    override fun offsetY() = (-1f + progress()) * dir.y

}
