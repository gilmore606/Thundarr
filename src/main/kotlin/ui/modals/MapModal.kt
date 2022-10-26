package ui.modals

import ui.input.Mouse

class MapModal : Modal(600, 600, "- yOUr tRAvELs -") {


    override fun mouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        dismiss()
        return true
    }

    override fun keyDown(keycode: Int) {
        super.keyDown(keycode)
        dismiss()
    }
}
