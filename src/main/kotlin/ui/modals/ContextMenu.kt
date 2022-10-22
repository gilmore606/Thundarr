package ui.modals

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import render.GameScreen

class ContextMenu(
    screenX: Int,
    screenY: Int
): SelectionModal(100, 50, null, Position.CURSOR, 0) {

    var parentModal: ParentModal? = null
    interface ParentModal {
        fun childSucceeded()
    }

    private val options = mutableMapOf<String,()->Unit>()
    private var maxOptionWidth = 0

    init {
        this.padding = 10
        this.spacing = 26
        this.animTime = 1f
        this.x = screenX
        this.y = screenY
        this.shadowOffset = 6
        this.borderWidth = 1
        this.headerPad = 16
    }

    fun addOption(text: String, handler: ()->Unit): ContextMenu {
        options[text] = handler
        val optionWidth = GlyphLayout(GameScreen.font, text).width.toInt()
        if (optionWidth > maxOptionWidth) {
            maxOptionWidth = optionWidth
            width = optionWidth + padding * 2
        }
        height = options.size * spacing + headerPad
        this.maxSelection = options.size - 1
        return this
    }

    override fun onResize(width: Int, height: Int) { }

    override fun drawModalText() {
        options.keys.forEachIndexed { n, text ->
            drawOptionText(text, n)
        }
    }

    override fun drawBackground() {
        super.drawBackground()
        drawOptionShade()
    }

    override fun doSelect() {
        dismiss()
        parentModal?.childSucceeded()
        GameScreen.clearCursor()
        options[options.keys.toList()[selection]]?.invoke()
    }

    override fun keyDown(keycode: Int) {
        when (keycode) {
            Input.Keys.TAB -> {
                parentModal?.childSucceeded()
                dismiss()
            }
            Input.Keys.NUMPAD_4 -> dismiss()
            else -> super.keyDown(keycode)
        }
    }
}
