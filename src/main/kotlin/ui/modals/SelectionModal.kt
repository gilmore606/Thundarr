package ui.modals

import com.badlogic.gdx.Input
import kotlinx.coroutines.selects.select
import render.GameScreen
import render.tilesets.Glyph
import ui.input.Mouse

abstract class SelectionModal(
    width: Int, height: Int, title: String? = null, position: Modal.Position = Position.LEFT,
    default: Int =  -1
) : Modal(width, height, title, position) {

    protected var selection = default
    protected var maxSelection = 1

    protected var headerPad = 70
    protected var padding = 15
    protected var spacing = 24
    protected var selectionBoxHeight = 20

    protected var mouseSelection = true

    protected fun selectNext() {
        selection++
        if (selection > maxSelection) selection = (if (maxSelection >= 0) maxSelection else -1)
    }

    protected fun selectPrevious() {
        selection--
        if (selection < 0) selection = maxSelection
    }

    abstract fun doSelect()

    protected fun drawOptionText(text: String, index: Int, spaceForIcon: Boolean = false) {
        drawString(text, padding + (if (spaceForIcon) 24 else 0), headerPad + spacing * index,
            if (index == selection) GameScreen.fontColorBold else GameScreen.fontColor)
    }

    protected fun drawOptionIcon(icon: Glyph, index: Int) {
        val x0 = this.x + padding - 10
        val y0 = this.y + headerPad + spacing * index - 12
        thingBatch.addPixelQuad(x0, y0, x0 + 32, y0 + 32, thingBatch.getTextureIndex(icon))
    }

    protected fun drawOptionShade() {
        if (!isAnimating() && selection >= 0) {
            drawSelectionBox(padding, headerPad + selection * spacing + 1, width - padding * 2 - 4, selectionBoxHeight)
        }
    }

    override fun keyDown(keycode: Int) {
        super.keyDown(keycode)
        when (keycode) {
            Input.Keys.NUMPAD_2, Input.Keys.DOWN -> { selectNext() }
            Input.Keys.NUMPAD_8, Input.Keys.UP -> { selectPrevious() }
            Input.Keys.SPACE, Input.Keys.NUMPAD_5, Input.Keys.ENTER, Input.Keys.NUMPAD_ENTER -> {
                doSelect()
            }
        }
    }

    override fun mouseClicked(screenX: Int, screenY: Int, button: Mouse.Button) {
        super.mouseClicked(screenX, screenY, button)
        mouseToOption(screenX, screenY)?.also { selection = it ; doSelect() }
    }

    override fun mouseMovedTo(screenX: Int, screenY: Int) {
        selection = mouseToOption(screenX, screenY) ?: -1
    }

    private fun mouseToOption(screenX: Int, screenY: Int): Int? {
        val localX = screenX - x
        val localY = screenY - y
        if (localX in 1 until width) {
            val hoverOption = (localY - headerPad) / spacing
            if (hoverOption in 0..maxSelection) {
                return hoverOption
            }
        }
        return null
    }
}
