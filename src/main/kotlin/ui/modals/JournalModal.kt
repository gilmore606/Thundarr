package ui.modals

import ui.input.Mouse

class JournalModal : Modal(600, 600, "- yOUr dEEDs -") {

    override fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        dismiss()
        return true
    }

    override fun onKeyDown(keycode: Int) {
        super.onKeyDown(keycode)
        dismiss()
    }
}
