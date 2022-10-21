package ui.modals

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import render.GameScreen
import ui.input.Mouse
import java.lang.Integer.min

class ContextMenu(
    screenX: Int,
    screenY: Int
): Modal(100, 50, null, Position.CURSOR) {

    private val options = mutableMapOf<String,()->Unit>()
    private var selection = 0
    private var maxOptionWidth = 0
    private val padding = 9
    private val spacing = 26

    init {
        this.animTime = 1f
        this.x = screenX
        this.y = screenY
        this.shadowOffset = 6
        this.borderWidth = 1
    }

    fun addOption(text: String, handler: ()->Unit): ContextMenu {
        options[text] = handler
        val optionWidth = GlyphLayout(GameScreen.font, text).width.toInt()
        if (optionWidth > maxOptionWidth) {
            maxOptionWidth = optionWidth
            width = optionWidth + padding * 2
        }
        height = options.size * spacing + padding * 2 - 6
        return this
    }

    override fun onResize(width: Int, height: Int) { }

    override fun drawModalText() {
        options.keys.forEachIndexed { n, text ->
            drawString(text, padding, n * spacing + padding + 2,
                if (selection == n) GameScreen.fontColorBold else GameScreen.fontColor)
        }
    }

    override fun drawBackground() {
        super.drawBackground()
        if (isAnimating()) return
        if (selection >= 0) {
            drawSelectionBox(12, selection * spacing + padding + 3, width - 24, spacing + 4)
        }
    }

    override fun mouseMovedTo(screenX: Int, screenY: Int) {
        mouseToOption(screenX, screenY)?.also { selection = it } ?: run { selection = -1 }
    }

    private fun mouseToOption(screenX: Int, screenY: Int): Int? {
        val localX = screenX - x
        val localY = screenY - y
        if (localX in 1 until width) {
            if (localY in 1 until height) {
                return min(options.size - 1, (localY - padding) / spacing)
            }
        }
        return null
    }

    override fun mouseClicked(screenX: Int, screenY: Int, button: Mouse.Button) {
        mouseToOption(screenX, screenY)?.also { selected ->
            selection = selected
            selectCurrentOption()
        }
    }

    private fun selectCurrentOption() {
        dismiss()
        GameScreen.clearCursor()
        options[options.keys.toList()[selection]]?.invoke()
    }

    override fun keyDown(keycode: Int) {
        super.keyDown(keycode)
        when (keycode) {
            Input.Keys.NUMPAD_2, Input.Keys.DOWN -> {
                selection++
                if (selection >= options.keys.size) selection = 0
            }
            Input.Keys.NUMPAD_8, Input.Keys.UP -> {
                selection--
                if (selection < 0) selection = options.keys.size - 1
            }
            Input.Keys.SPACE, Input.Keys.NUMPAD_5, Input.Keys.ENTER, Input.Keys.NUMPAD_ENTER -> {
                selectCurrentOption()
            }
        }
    }
}
