package ui.modals

import com.badlogic.gdx.Input
import render.GameScreen

class EscMenu : Modal(270, 230, "- THUNDARR -") {

    val options = listOf("Restart world", "Settings", "Controls", "Credits", "Save and quit")
    var selection: Int = -1
    val optionSpacing = GameScreen.fontSize + 12
    val headerSpacing = 64

    override fun drawText() {
        super.drawText()
        options.forEachIndexed { n, optionText ->
            drawString(optionText, 56, headerSpacing + n * optionSpacing,
                if (n == selection) GameScreen.fontColorBold else GameScreen.fontColor)
        }
    }

    override fun keyDown(keycode: Int) {
        super.keyDown(keycode)
        when (keycode) {
            Input.Keys.NUMPAD_2, Input.Keys.DOWN -> {
                selection++
                if (selection > options.lastIndex) selection = 0
            }
            Input.Keys.NUMPAD_8, Input.Keys.UP -> {
                selection--
                if (selection < 0) selection = options.lastIndex
            }
            Input.Keys.SPACE, Input.Keys.NUMPAD_5, Input.Keys.ENTER, Input.Keys.NUMPAD_ENTER -> {
                selectCurrentOption()
            }
        }
    }

    override fun mouseMovedTo(screenX: Int, screenY: Int) {
        val hoverOption = mouseToOption(screenX, screenY)
        if (hoverOption >= 0) {
            selection = hoverOption
        }
    }

    override fun mouseClicked(screenX: Int, screenY: Int) {
        if (mouseToOption(screenX, screenY) >= 0) {
            selectCurrentOption()
        }
    }

    private fun selectCurrentOption() {
        dismiss()
    }

    private fun mouseToOption(screenX: Int, screenY: Int): Int {
        val localX = screenX - x
        val localY = screenY - y
        if (localX > 0 && localX < width) {
            val hoverOption = (localY - headerSpacing) / optionSpacing
            if (hoverOption >= 0 && hoverOption <= options.lastIndex) {
                return hoverOption
            }
        }
        return -1
    }
}
