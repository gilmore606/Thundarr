package ui.modals

import render.batches.QuadBatch
import ui.input.Mouse

class MapModal : Modal(600, 600, "- yOUr tRAvELs -") {


    override fun newThingBatch() = null
    override fun newActorBatch() = null

    override fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        dismiss()
        return true
    }

    override fun onKeyDown(keycode: Int) {
        super.onKeyDown(keycode)
        dismiss()
    }
}
