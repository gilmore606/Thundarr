package ui.modals

import audio.Speaker
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import render.Screen
import things.Thing
import ui.input.Keyboard
import ui.input.Mouse
import util.NORTH
import util.NO_DIRECTION
import util.SOUTH
import util.WEST
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
        changeSelection(-1)
    }

    protected fun optionX(n: Int) = this.x + padding
    protected fun optionY(n: Int) = this.y + headerPad + spacing * n

    protected open fun selectNext() {
        changeSelection(if (selection >= maxSelection) if (maxSelection < 0) -1 else 0 else selection + 1)
    }

    protected open fun selectPrevious() {
        changeSelection(if (selection < 1) maxSelection else selection - 1)
    }

    open fun changeSelection(newSelection: Int) {
        if (newSelection != selection) {
            Speaker.ui(Speaker.SFX.UIMOVE, screenX = x)
        }
        selection = newSelection
    }

    open fun doSelect() {
        Speaker.ui(Speaker.SFX.UISELECT, screenX = x)
    }

    protected fun drawOptionText(text: String, index: Int, preSpace: Int = 0,
                                 colorOverride: Color? = null, addCol: String? = null, colX: Int = 0,
                                 addTag: String? = null) {
        val lx = padding + preSpace
        drawString(text, lx, headerPad + spacing * index - 2,
            colorOverride ?: if (index == selection) Screen.fontColorBold else Screen.fontColor)
        addCol?.also { addCol ->
            drawString(addCol, lx + colX, headerPad + spacing * index - 2,
                Screen.fontColorDull, Screen.smallFont)
        }
        addTag?.also { tag ->
            val x = measure(text) + 6
            drawString(tag, padding + preSpace + x, headerPad + spacing * index - 1, Screen.fontColorDull, Screen.smallFont)
        }
    }

    protected fun drawOptionIcon(entity: Entity, index: Int) {
        val x0 = this.x + padding - 10
        val y0 = this.y + headerPad + spacing * index - 12
        val batch = if (entity is Thing) myThingBatch() else myActorBatch()
        batch?.addPixelQuad(x0, y0, x0 + 32, y0 + 32, batch.getTextureIndex(entity.glyph()), hue = entity.hue())
    }

    protected fun drawOptionShade(space: Int = 0) {
        if (!isAnimating() && selection >= 0) {
            drawSelectionBox(padding + space, headerPad + selection * spacing + 1, width - padding * 2 - 4 - space, selectionBoxHeight)
        }
    }

    override fun onKeyDown(keycode: Int) {
        super.onKeyDown(keycode)
        if (Keyboard.moveKeys[keycode] == NORTH) {
            selectPrevious()
        } else if (Keyboard.moveKeys[keycode] == SOUTH) {
            selectNext()
        } else if (Keyboard.moveKeys[keycode] == NO_DIRECTION || keycode in listOf(Input.Keys.SPACE, Input.Keys.ENTER)) {
            if (selection >= 0) doSelect()
        } else when (keycode) {
            Input.Keys.NUM_1 -> onShortcutSelect(0)
            Input.Keys.NUM_2 -> onShortcutSelect(1)
            Input.Keys.NUM_3 -> onShortcutSelect(2)
            Input.Keys.NUM_4 -> onShortcutSelect(3)
            Input.Keys.NUM_5 -> onShortcutSelect(4)
            Input.Keys.NUM_6 -> onShortcutSelect(5)
            Input.Keys.NUM_7 -> onShortcutSelect(6)
            Input.Keys.NUM_8 -> onShortcutSelect(7)
            Input.Keys.NUM_9 -> onShortcutSelect(8)
        }
    }

    private fun onShortcutSelect(newSelection: Int) {
        if (newSelection <= maxSelection) {
            KtxAsync.launch {
                changeSelection(newSelection)
                delay(100L)
                doSelect()
            }
        }
    }

    override fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        if (super.onMouseClicked(screenX, screenY, button)) return true
        mouseToOption(screenX, screenY)?.also { changeSelection(it) ; doSelect(); return true }
        return false
    }

    override fun onMouseMovedTo(screenX: Int, screenY: Int) {
        changeSelection(mouseToOption(screenX, screenY) ?: -1)
        if (selection > 0) {
            if (isInSidecar) {
                returnFromSidecar()
            }
        }
    }

    protected open fun mouseToOption(screenX: Int, screenY: Int): Int? {
        val localX = screenX - x
        val localY = screenY - y + (spacing / 2)
        if (localX in 1 until width) {
            val hoverOption = (localY - headerPad - 5) / spacing
            if (hoverOption in 0..maxSelection) {
                return hoverOption
            }
        }
        return null
    }
}
