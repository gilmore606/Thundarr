package actors.animations

import ui.input.Keyboard
import util.XY

class Step(
    val dir: XY
) : Animation(Keyboard.REPEAT_MS) {

    override fun offsetX() = (-1f + progress()) * dir.x
    override fun offsetY(): Float {
        val p = progress()
        return (-1f + p) * dir.y - (if (p < 0.5f) p * 0.3f else (1f - p) * 0.3f)
    }

}
