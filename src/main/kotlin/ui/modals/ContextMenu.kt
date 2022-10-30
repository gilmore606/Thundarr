package ui.modals

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import render.Screen

class ContextMenu(
    screenX: Int,
    screenY: Int,
    private val onHover: ((Int)->Unit)? = null
): SelectionModal(100, 50, null, Position.CURSOR, 0) {

    var parentModal: ParentModal? = null
    interface ParentModal {
        fun childSucceeded()
    }

    val options = mutableMapOf<String,()->Unit>()
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
        val optionWidth = GlyphLayout(Screen.font, text).width.toInt()
        if (optionWidth > maxOptionWidth) {
            maxOptionWidth = optionWidth
            width = optionWidth + padding * 2
        }
        height = options.size * spacing + headerPad
        this.maxSelection = options.size - 1
        return this
    }

    override fun onResize(width: Int, height: Int) { }

    override fun onMouseMovedTo(screenX: Int, screenY: Int) {
        super.onMouseMovedTo(screenX, screenY)
        onHover?.invoke(selection)
    }

    override fun selectNext() {
        super.selectNext()
        onHover?.invoke(selection)
    }

    override fun selectPrevious() {
        super.selectPrevious()
        onHover?.invoke(selection)
    }

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
        Screen.clearCursor()
        options[options.keys.toList()[selection]]?.invoke()
    }

    override fun onKeyDown(keycode: Int) {
        when (keycode) {
            Input.Keys.TAB -> {
                parentModal?.childSucceeded()
                dismiss()
            }
            Input.Keys.NUMPAD_4 -> dismiss()
            else -> super.onKeyDown(keycode)
        }
    }
}
