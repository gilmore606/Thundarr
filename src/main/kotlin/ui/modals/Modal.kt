package ui.modals

import com.badlogic.gdx.Input
import render.GameScreen
import ui.panels.Panel
import render.tilesets.Glyph
import ui.input.Mouse
import java.lang.Float.min

abstract class Modal(
    width: Int,
    height: Int,
    val title: String? = null,
    val position: Position = Position.LEFT
) : Panel() {
    enum class Position { LEFT, CENTER_LOW, CURSOR }

    private var dismissible = true
    private val launchTimeMs = System.currentTimeMillis()
    protected var animTime = 80f
    protected fun isAnimating() = (System.currentTimeMillis() - launchTimeMs) < animTime

    init {
        this.width = width
        this.height = height
    }

    override fun onResize(width: Int, height: Int) {
        this.x = 40
        this.y = (height - this.height) / 2
    }

    override fun drawBackground() {
        val anim = min(1f, (System.currentTimeMillis() - launchTimeMs) / animTime)
        val xSquish = ((1f - anim) * width / 2f).toInt()
        val ySquish = ((1f - anim) * height / 2f).toInt()
        drawBox(x - xSquish, y + ySquish, width - xSquish * 2, height - ySquish * 2)
    }

    override fun drawText() {
        if (!isAnimating()) {
            title?.also { title ->
                drawTitle(title)
            }
            drawModalText()
        }
    }

    open fun drawModalText() { }

    open fun keyDown(keycode: Int) {
        if (dismissible && keycode == Input.Keys.ESCAPE) {
            dismiss()
        }
    }

    open fun keyUp(keycode: Int) {

    }

    open fun mouseMovedTo(screenX: Int, screenY: Int) {

    }

    open fun mouseClicked(screenX: Int, screenY: Int, button: Mouse.Button) {

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
