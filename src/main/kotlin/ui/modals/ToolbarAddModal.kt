package ui.modals

import things.Thing

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

    fun remoteClose() {
        dismissible = true
        dismiss()
    }
}
