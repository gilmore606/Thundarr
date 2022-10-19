package ui.modals

import com.badlogic.gdx.graphics.g2d.GlyphLayout
import render.GameScreen
import ui.input.Mouse
import java.lang.Integer.min

class ContextMenu(
    screenX: Int,
    screenY: Int
): Modal(100, 50, null, Position.CURSOR) {

    private val options = mutableMapOf<String,()->Unit>()
    private var selection = -1
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
                return min(options.size, (localY - padding) / spacing)
            }
        }
        return null
    }

    override fun mouseClicked(screenX: Int, screenY: Int, button: Mouse.Button) {
        dismiss()
        mouseToOption(screenX, screenY)?.also { selected ->
            options[options.keys.toList().get(selected)]?.invoke()
        }
    }

    override fun keyDown(keycode: Int) {
        dismiss()
    }
}
