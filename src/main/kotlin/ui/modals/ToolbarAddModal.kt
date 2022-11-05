package ui.modals

import com.badlogic.gdx.Input
import things.Thing
import ui.panels.Toolbar

class ToolbarAddModal(
    val newThing: Thing,
    val message: String,
    val sampleText: String
) : Modal(260, 120, null, Position.CENTER_LOW) {

    private val padding = 24

    override fun onResize(width: Int, height: Int) {
        this.width = measure(message) + padding * 2
        this.height = padding * 2 + 20
        super.onResize(width, height)
    }

    override fun drawModalText() {
        drawString(message, padding, padding)
    }

    override fun onKeyDown(keycode: Int) {
        if (keycode in listOf(Input.Keys.NUM_1, Input.Keys.NUM_2, Input.Keys.NUM_3, Input.Keys.NUM_4,
                Input.Keys.NUM_5, Input.Keys.NUM_6, Input.Keys.NUM_7, Input.Keys.NUM_8)) {
            Toolbar.onKey(keycode - Input.Keys.NUM_1 + 1)
        }
    }

    fun remoteClose() {
        dismissible = true
        dismiss()
    }
}
