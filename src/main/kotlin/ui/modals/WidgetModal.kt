package ui.modals

import ui.input.Mouse
import ui.modals.widgets.Widget

abstract class WidgetModal(
    width: Int, height: Int, title: String? = null, position: Modal.Position = Modal.Position.LEFT
) : Modal(width, height, title, position) {

    val widgets = ArrayList<Widget>()

    fun add(widget: Widget) { widgets.add(widget) }

    override fun drawBackground() {
        super.drawBackground()
        if (isAnimating()) return
        widgets.forEach { it.draw() }
    }

    override fun drawModalText() {
        super.drawModalText()
        widgets.forEach { it.drawText() }
    }

    override fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        widgets.forEach { widget ->
            val relx = screenX - this.x - widget.x
            val rely = screenY - this.y - widget.y
            if (relx >= 0 && relx < widget.width && rely >= 0 && rely < widget.height) {
                widget.onMouseClicked(relx, rely, button)
                return true
            }
        }
        return false
    }

    override fun onMouseMovedTo(screenX: Int, screenY: Int) {
        widgets.forEach { widget ->
            val relx = screenX - this.x - widget.x
            val rely = screenY - this.y - widget.y
            if (relx >= 0 && relx < widget.width && rely >= 0 && rely < widget.height) {
                widget.onMouseMovedTo(relx, rely)
            }
        }
    }

    override fun onMouseUp(screenX: Int, screenY: Int, button: Mouse.Button) {
        widgets.forEach { widget ->
            val relx = screenX - this.x - widget.x
            val rely = screenY - this.y - widget.y
            if (relx >= 0 && relx < widget.width && rely >= 0 && rely < widget.height) {
                widget.onMouseUp(relx, rely, button)
            }
        }
    }
}
