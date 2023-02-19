package ui.modals

import com.badlogic.gdx.Input
import things.Thing
import ui.input.Keydef
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

    override fun onKeyDown(key: Keydef) {
        when (key) {
            Keydef.SHORTCUT1 -> Toolbar.onKey(1)
            Keydef.SHORTCUT2 -> Toolbar.onKey(2)
            Keydef.SHORTCUT3 -> Toolbar.onKey(3)
            Keydef.SHORTCUT4 -> Toolbar.onKey(4)
            Keydef.SHORTCUT5 -> Toolbar.onKey(5)
            Keydef.SHORTCUT6 -> Toolbar.onKey(6)
            Keydef.SHORTCUT7 -> Toolbar.onKey(7)
            Keydef.SHORTCUT8 -> Toolbar.onKey(8)
            Keydef.SHORTCUT9 -> Toolbar.onKey(9)
            else -> { }
        }
    }

    fun remoteClose() {
        dismissible = true
        dismiss()
    }
}
