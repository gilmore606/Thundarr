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
import ui.input.Keydef
import ui.input.Mouse
import util.*
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

    override fun moveToSidecar() {
        super.moveToSidecar()
        sidecar?.also { if (it is SelectionModal && it.selection == -1) it.selection = min(it.maxSelection, selection) }
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
                                 addTag: String? = null, tagColorOverride: Color? = null) {
        val lx = padding + preSpace
        drawString(text, lx, headerPad + spacing * index - 2,
            colorOverride ?: if (index == selection) Screen.fontColorBold else Screen.fontColor)
        addCol?.also { addCol ->
            drawString(addCol, lx + colX, headerPad + spacing * index - 2,
                tagColorOverride ?: Screen.fontColorDull, Screen.font)
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

    protected fun drawOptionShade(space: Int = 0, forceY: Int? = null, rightSpace: Int = 0) {
        if (!isAnimating() && selection >= 0) {
            drawSelectionBox(padding + space,
                forceY ?: (headerPad + selection * spacing + 1), width - padding * 2 - 4 - space - rightSpace, selectionBoxHeight)
        }
    }

    override fun onKeyDown(key: Keydef) {
        super.onKeyDown(key)
        when (key) {
            Keydef.MOVE_N -> selectPrevious()
            Keydef.MOVE_S -> selectNext()
            Keydef.INTERACT -> doSelect()
            Keydef.SHORTCUT1 -> onShortcutSelect(0)
            Keydef.SHORTCUT2 -> onShortcutSelect(1)
            Keydef.SHORTCUT3 -> onShortcutSelect(2)
            Keydef.SHORTCUT4 -> onShortcutSelect(3)
            Keydef.SHORTCUT5 -> onShortcutSelect(4)
            Keydef.SHORTCUT6 -> onShortcutSelect(5)
            Keydef.SHORTCUT7 -> onShortcutSelect(6)
            Keydef.SHORTCUT8 -> onShortcutSelect(7)
            Keydef.SHORTCUT9 -> onShortcutSelect(8)
            else -> { }
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
        doSelect()
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
