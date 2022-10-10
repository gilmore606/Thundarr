package ui.modals

import com.badlogic.gdx.Input
import render.GameScreen
import ui.Panel

abstract class Modal(
    width: Int,
    height: Int
) : Panel() {

    init {
        this.width = width
        this.height = height
        onResize(GameScreen.width, GameScreen.height)
    }

    override fun onResize(width: Int, height: Int) {
        this.x = (width - this.width) / 2
        this.y = (height - this.height) / 2
    }

    override fun drawBackground() {
        drawBox(x, y, width, height)
    }

    override fun drawText() {

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

    private fun dismiss() {
        GameScreen.dismissModal(this)
    }
}
