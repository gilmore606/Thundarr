package ui.modals

import ui.input.Mouse

class JournalModal : Modal(600, 600, "- yOUr dEEDs -") {

    override fun mouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        dismiss()
        return true
    }

    override fun keyDown(keycode: Int) {
        super.keyDown(keycode)
        dismiss()
    }
}
