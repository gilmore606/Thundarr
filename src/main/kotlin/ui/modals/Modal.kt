package ui.modals

import com.badlogic.gdx.Input
import javafx.scene.input.MouseButton
import render.Screen
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
    enum class Position { LEFT, TOP, RIGHT, BOTTOM, CENTER_LOW, CURSOR, SIDECAR }

    var dismissible = true
    private val launchTimeMs = System.currentTimeMillis()
    protected var animTime = 80f
    protected fun isAnimating() = (System.currentTimeMillis() - launchTimeMs) < animTime
    var dismissOnClickOutside = true

    var sidecar: Modal? = null
    var isSidecar = false
    var isInSidecar = false

    init {
        this.width = width
        this.height = height
    }

    open fun myXmargin() = xMargin

    open fun moveToSidecar() { sidecar?.also { isInSidecar = true } }
    open fun returnFromSidecar() { isInSidecar = false }

    override fun onResize(width: Int, height: Int) {
        val myXmargin = myXmargin()
        if (position == Position.LEFT || position == Position.SIDECAR) {
            this.x = 40 + myXmargin
        } else if (position == Position.RIGHT) {
            this.x = width - xMargin - 40
        } else {
            this.x = (width - this.width) / 2
        }
        if (position == Position.TOP) {
            this.y = 40 + yMargin
        } else if (position == Position.BOTTOM) {
            this.y = height - yMargin - 40
        } else if (position == Position.CENTER_LOW) {
            this.y = (height - this.height) / 2 + 100
        } else {
            this.y = (height - this.height) / 2
        }
        sidecar?.onResize(width, height)
    }

    override fun drawBackground() {
        val anim = min(1f, (System.currentTimeMillis() - launchTimeMs) / animTime)
        val xSquish = ((1f - anim) * width / 2f).toInt()
        val ySquish = ((1f - anim) * height / 2f).toInt()
        drawBox(x - xSquish, y + ySquish, width - xSquish * 2, height - ySquish * 2)
        sidecar?.drawBackground()
    }

    override fun drawText() {
        if (!isAnimating()) {
            title?.also { title ->
                drawTitle(title)
            }
            drawModalText()
        }
        sidecar?.drawText()
    }

    open fun drawModalText() { }

    fun keyDown(keycode: Int) {
        if (dismissible && keycode == Input.Keys.ESCAPE) {
            dismiss()
            return
        }
        if (isInSidecar) sidecar?.also { it.onKeyDown(keycode) } ?: run { onKeyDown(keycode) }
        else onKeyDown(keycode)
    }
    open fun onKeyDown(keycode: Int) { }

    fun keyUp(keycode: Int) {
        if (isInSidecar) sidecar?.also { it.onKeyUp(keycode) } ?: run { onKeyUp(keycode) }
        else onKeyUp(keycode)
    }
    open fun onKeyUp(keycode: Int) { }

    final override fun mouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean =
        if (isInSidecar) (sidecar?.onMouseClicked(screenX, screenY, button) ?: onMouseClicked(screenX, screenY, button))
        else onMouseClicked(screenX, screenY, button)

    open fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        if (screenX < this.x || screenX > (this.x + width) || screenY < this.y || screenY > this.y + height) {
            if (dismissOnClickOutside && dismissible) {
                dismiss()
                return true
            }
        }
        return false
    }

    fun mouseUp(screenX: Int, screenY: Int, button: Mouse.Button) {
        onMouseUp(screenX, screenY, button)
        sidecar?.onMouseUp(screenX, screenY, button)
    }
    open fun onMouseUp(screenX: Int, screenY: Int, button: Mouse.Button) { }

    final override fun mouseMovedTo(screenX: Int, screenY: Int) {
        onMouseMovedTo(screenX, screenY)
        sidecar?.onMouseMovedTo(screenX, screenY)
    }
    open fun onMouseMovedTo(screenX: Int, screenY: Int) { }

    protected fun dismiss() {
        dismissed = true
        sidecar?.dismiss()
        onDismiss()
    }
    open fun onDismiss() { }

    protected fun drawSelectionBox(x0: Int, y0: Int, width: Int, height: Int) {
        Screen.uiBatch.addPixelQuad(this.x + x0 - 6, this.y + y0 - (7 + height / 4),
            this.x + x0 + width + 12, this.y + y0 + height,
            Screen.uiBatch.getTextureIndex(Glyph.BOX_SHADOW))
    }

    open fun advanceTime(turns: Float) { sidecar?.advanceTime(turns) }
}
