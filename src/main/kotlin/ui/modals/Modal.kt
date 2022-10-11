package ui.modals

import com.badlogic.gdx.Input
import render.GameScreen
import ui.Panel

abstract class Modal(
    width: Int,
    height: Int,
    val title: String? = null
) : Panel() {

    init {
        this.width = width
        this.height = height
    }

    override fun onResize(width: Int, height: Int) {
        this.x = (width - this.width) / 2
        this.y = (height - this.height) / 2
    }

    override fun drawBackground() {
        drawBox(x, y, width, height)
    }

    override fun drawText() {
        title?.also { title ->
            drawTitle(title)
        }
    }

    open fun keyDown(keycode: Int) {
        if (keycode == Input.Keys.ESCAPE) {
            dismiss()
        }
    }

    open fun mouseMovedTo(screenX: Int, screenY: Int) {

    }

    open fun mouseClicked(screenX: Int, screenY: Int) {

    }

    protected fun dismiss() {
        GameScreen.dismissModal(this)
    }
}
