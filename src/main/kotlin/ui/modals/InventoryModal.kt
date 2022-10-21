package ui.modals

import ui.input.Mouse

class InventoryModal : Modal(400, 700, "- bACkPACk -") {


    override fun mouseClicked(screenX: Int, screenY: Int, button: Mouse.Button) {
        dismiss()
    }

    override fun keyDown(keycode: Int) {
        super.keyDown(keycode)
        dismiss()
    }

}
