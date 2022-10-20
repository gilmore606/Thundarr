package ui.modals

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import render.GameScreen
import ui.input.Mouse

class ConfirmModal(
    val text: List<String>,
    val yesText: String = "OK",
    val noText: String = "Cancel",
    val callback: (Boolean)->Unit
): Modal(
    textWidth(text) + 48,
    text.size * 24 + 90,
    position = Position.LEFT
) {

    companion object {
        fun textWidth(text: List<String>): Int = text.maxOf { GlyphLayout(GameScreen.font, it).width }.toInt()
    }

    var selection: Boolean = false

    val yesOffset: Int
    val noOffset: Int
    val yesWidth: Int
    val noWidth: Int

    init {
        yesWidth = GlyphLayout(GameScreen.font, yesText).width.toInt()
        noWidth = GlyphLayout(GameScreen.font, noText).width.toInt()
        val spaceWidth = (width - 48) - (yesWidth + noWidth)
        yesOffset = spaceWidth / 3
        noOffset = yesOffset + yesWidth + spaceWidth / 3
    }

    override fun drawModalText() {
        var iy = 24
        text.forEach { line ->
            drawString(line, 24, iy, GameScreen.fontColorBold)
            iy += 24
        }
        iy += 20
        drawString(yesText, 24 + yesOffset, iy,
            if (selection) GameScreen.fontColorBold else GameScreen.fontColor)
        drawString(noText, 24 + noOffset, iy,
            if (!selection) GameScreen.fontColorBold else GameScreen.fontColor)
    }

    override fun drawBackground() {
        super.drawBackground()
        if (isAnimating()) return
        val iy = 24 + text.size * 24 + 20
        if (selection) {
            drawSelectionBox(24 + yesOffset, iy, yesWidth, 28)
        } else {
            drawSelectionBox(24 + noOffset, iy, noWidth, 28)
        }
    }

    override fun keyDown(keycode: Int) {
        super.keyDown(keycode)
        when (keycode) {
            Input.Keys.NUMPAD_8, Input.Keys.NUMPAD_2, Input.Keys.NUMPAD_4, Input.Keys.NUMPAD_6, Input.Keys.DOWN,
            Input.Keys.UP, Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.TAB -> {
                selection = !selection
            }
            Input.Keys.Y -> { selection = true }
            Input.Keys.N -> { selection = false }
            Input.Keys.SPACE, Input.Keys.NUMPAD_5, Input.Keys.ENTER, Input.Keys.NUMPAD_ENTER -> {
                selectCurrentOption()
            }
        }
    }

    override fun mouseMovedTo(screenX: Int, screenY: Int) {
        mouseToOption(screenX, screenY)?.also { selection = it }
    }

    override fun mouseClicked(screenX: Int, screenY: Int, button: Mouse.Button) {
        mouseToOption(screenX, screenY)?.also { selected ->
            selection = selected
            selectCurrentOption()
        }
    }

    private fun selectCurrentOption() {
        dismiss()
        callback.invoke(selection)
    }

    private fun mouseToOption(screenX: Int, screenY: Int): Boolean? {
        val localX = screenX - x
        val localY = screenY - y
        if (localX in 1 until width) {
            if (localY in 1 until height) {
                if (localX in yesOffset until noOffset) {
                    return true
                }
                if (localX >= noOffset) {
                    return false
                }
            }
        }
        return null
    }
}
