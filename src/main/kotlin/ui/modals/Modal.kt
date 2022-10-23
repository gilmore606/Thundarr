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
    protected var dismissOnClickOutside = true

    init {
        this.width = width
        this.height = height
    }

    override fun onResize(width: Int, height: Int) {
        if (position == Position.LEFT) {
            this.x = 40
        } else {
            this.x = (width - this.width) / 2
        }
        if (position == Position.CENTER_LOW) {
            this.y = (height - this.height) / 2 + 100
        }
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

    override fun mouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        if (screenX < this.x || screenX > (this.x + width) || screenY < this.y || screenY > this.y + height) {
            if (dismissOnClickOutside) {
                dismiss()
                return true
            }
        }
        return false
    }

    protected fun dismiss() {
        GameScreen.dismissModal(this)
    }

    protected fun drawSelectionBox(x0: Int, y0: Int, width: Int, height: Int) {
        boxBatch.addPixelQuad(this.x + x0 - 6, this.y + y0 - (7 + height / 4),
            this.x + x0 + width + 12, this.y + y0 + height,
            boxBatch.getTextureIndex(Glyph.BOX_SHADOW))
    }
}
