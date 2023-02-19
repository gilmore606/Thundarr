package ui.modals

import render.Screen
import ui.input.Keyboard
import ui.input.Keydef
import util.XY
import java.lang.Integer.max

class DirectionModal(
    val message: String,
    val callback: (XY)->Unit
) : Modal(260, 120, null, Position.CENTER_LOW) {

    private val padding = 24
    private val hint = "(Press a direction, or ESC to cancel)"

    override fun onResize(width: Int, height: Int) {
        this.width = max(measure(hint, Screen.smallFont), measure(message)) + padding * 2
        this.height = padding * 2 + 40
        super.onResize(width, height)
    }

    override fun drawModalText() {
        drawCenterText(message, 0, padding, width)
        drawCenterText(hint, 0, padding + 26, width, Screen.fontColorDull, Screen.smallFont)
    }

    override fun onKeyDown(key: Keydef) {
        Keyboard.moveKeys[key]?.also { dir ->
            dismiss()
            callback(dir)
        } ?: run {
            dismiss()
        }
    }
}
