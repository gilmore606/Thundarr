package ui.modals

import com.badlogic.gdx.Input
import render.GameScreen
import ui.panels.Panel
import render.tilesets.Glyph

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

    open fun keyUp(keycode: Int) {

    }

    open fun mouseMovedTo(screenX: Int, screenY: Int) {

    }

    open fun mouseClicked(screenX: Int, screenY: Int) {

    }

    protected fun dismiss() {
        GameScreen.dismissModal(this)
    }

    protected fun drawSelectionBox(x0: Int, y0: Int, width: Int, height: Int) {
        boxBatch.addPixelQuad(this.x + x0 - 9, this.y + y0 - 9,
            this.x + x0 + width + 9, this.y + y0 + height - 8,
            boxBatch.getTextureIndex(Glyph.BOX_SHADOW))
    }
}
