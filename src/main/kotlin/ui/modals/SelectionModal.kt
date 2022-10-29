package ui.modals

import com.badlogic.gdx.Input
import render.Screen
import things.Thing
import ui.input.Mouse
import world.Entity
import java.lang.Integer.min

abstract class SelectionModal(
    width: Int, height: Int, title: String? = null, position: Modal.Position = Position.LEFT,
    default: Int =  -1
) : Modal(width, height, title, position) {

    var selection = default
    var maxSelection = 1

    protected var headerPad = 70
    protected var padding = 15
    protected var spacing = 24
    protected var selectionBoxHeight = 20

    protected var mouseSelection = true

    override fun moveToSidecar() {
        super.moveToSidecar()
        sidecar?.also { if (it is SelectionModal) it.selection = min(it.maxSelection, selection) }
        selection = -1
    }

    protected fun optionX(n: Int) = this.x + padding
    protected fun optionY(n: Int) = this.y + headerPad + spacing * n

    protected fun selectNext() {
        selection++
        if (selection > maxSelection) selection = (if (maxSelection >= 0) 0 else -1)
    }

    protected fun selectPrevious() {
        selection--
        if (selection < 0) selection = maxSelection
    }

    abstract fun doSelect()

    protected fun drawOptionText(text: String, index: Int, spaceForIcon: Boolean = false) {
        drawString(text, padding + (if (spaceForIcon) 28 else 0), headerPad + spacing * index - 2,
            if (index == selection) Screen.fontColorBold else Screen.fontColor)
    }

    protected fun drawOptionIcon(entity: Entity, index: Int) {
        val x0 = this.x + padding - 10
        val y0 = this.y + headerPad + spacing * index - 12
        val batch = if (entity is Thing) myThingBatch() else myActorBatch()
        batch.addPixelQuad(x0, y0, x0 + 32, y0 + 32, batch.getTextureIndex(entity.glyph()))
    }

    protected fun drawOptionShade() {
        if (!isAnimating() && selection >= 0) {
            drawSelectionBox(padding, headerPad + selection * spacing + 1, width - padding * 2 - 4, selectionBoxHeight)
        }
    }

    override fun onKeyDown(keycode: Int) {
        super.onKeyDown(keycode)
        when (keycode) {
            Input.Keys.NUMPAD_2, Input.Keys.DOWN, Input.Keys.X -> { selectNext() }
            Input.Keys.NUMPAD_8, Input.Keys.UP, Input.Keys.W -> { selectPrevious() }
            Input.Keys.SPACE, Input.Keys.NUMPAD_5, Input.Keys.ENTER, Input.Keys.NUMPAD_ENTER, Input.Keys.S -> {
                if (selection >= 0 ) doSelect()
            }
        }
    }

    override fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        if (button != Mouse.Button.LEFT) return false
        if (super.onMouseClicked(screenX, screenY, button)) return true
        mouseToOption(screenX, screenY)?.also { selection = it ; doSelect(); return true }
        return false
    }

    override fun onMouseMovedTo(screenX: Int, screenY: Int) {
        selection = mouseToOption(screenX, screenY) ?: -1
        if (selection > 0) {
            if (isInSidecar) {
                returnFromSidecar()
            }
        }
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
