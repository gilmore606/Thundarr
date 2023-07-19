package ui.modals

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import render.Screen
import render.batches.QuadBatch
import render.tilesets.UITileSet
import ui.input.Keyboard
import ui.input.Keydef
import util.WEST

class ContextMenu(
    screenX: Int,
    screenY: Int,
    private val onHover: ((Int)->Unit)? = null
): SelectionModal(0, 0, null, Position.CURSOR, 0) {

    companion object {
        val boxBatch = QuadBatch(Screen.uiTileSet)
    }
    override fun openSound() = null
    override fun closeSound() = null

    override fun newBoxBatch() = ContextMenu.boxBatch
    override fun newThingBatch() = null
    override fun newActorBatch() = null

    class Option(val name: String, val onPick: ()->Unit)

    var parentModal: ParentModal? = null
    interface ParentModal {
        fun childSucceeded()
        fun childCancelled() { }
    }
    var succeeded = false

    val options = mutableListOf<Option>()
    var maxOptionWidth = 0

    init {
        this.padding = 10
        this.spacing = 26
        this.animTime = 1f
        this.x = screenX
        this.y = screenY
        this.shadowOffset = 6
        this.borderWidth = 1
        this.headerPad = 16
        this.darkenUnder = true
        this.darkenUnderSidecar = false
        changeSelection(0)
    }

    fun addOption(text: String, handler: ()->Unit): ContextMenu {
        options.add(Option(text, handler))
        val optionWidth = GlyphLayout(Screen.font, text).width.toInt()
        if (optionWidth > maxOptionWidth) {
            maxOptionWidth = optionWidth
            width = optionWidth + padding * 2 + 20
        }
        height = options.size * spacing + headerPad
        this.maxSelection = options.size - 1
        return this
    }

    override fun onResize(width: Int, height: Int) { }

    override fun changeSelection(newSelection: Int) {
        super.changeSelection(newSelection)
        onHover?.invoke(newSelection)
    }

    override fun drawModalText() {
        options.forEachIndexed { n, opt ->
            if (n < 9) {
                drawOptionText(opt.name, n, preSpace = 18, colX = -18, addCol = (n + 1).toString())
            } else {
                drawOptionText(opt.name, n, preSpace = 18)
            }
        }
    }

    override fun drawBackground() {
        super.drawBackground()
        drawOptionShade()
    }

    override fun doSelect() {
        if (selection < 0) return
        super.doSelect()
        dismissSuccess()
        options[selection].onPick.invoke()
    }

    override fun onKeyDown(key: Keydef) {
        if (key == Keydef.OPEN_INV) dismissSuccess()
        else if (key == Keydef.MOVE_W) {
            dismiss()
        } else {
            super.onKeyDown(key)
        }
    }

    override fun onDismiss() {
        super.onDismiss()
        if (!succeeded) {
            parentModal?.childCancelled()
        } else {
            parentModal?.childSucceeded()
        }
    }

    private fun dismissSuccess() {
        succeeded = true
        dismiss()
    }

    override fun dispose() {
        textBatch.dispose()
    }

}
