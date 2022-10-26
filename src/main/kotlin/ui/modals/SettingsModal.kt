package ui.modals

import ui.input.Mouse

class SettingsModal : Modal(300, 500, "- settings -") {

    private val padding = 22

    override fun mouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        dismiss()
        return true
    }

    override fun keyDown(keycode: Int) {
        super.keyDown(keycode)
        dismiss()
    }
}
