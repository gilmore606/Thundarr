package ui.modals

import com.badlogic.gdx.Input
import render.GameScreen

class EscMenu : Modal(270, 230, "- THUNDARR -") {

    private val options = LinkedHashMap<String, ()->Unit>().apply {
        put("Restart world") { App.restartWorld() }
        put("Settings") { App.openSettings() }
        put("Controls") { App.openControls() }
        put("Credits") { App.openCredits() }
        put("Save and quit") { App.saveAndQuit() }
    }
    var selection: Int = 0
    private val optionSpacing = GameScreen.fontSize + 12
    private val headerSpacing = 64

    override fun drawText() {
        super.drawText()
        options.keys.forEachIndexed { n, optionText ->
            drawString(optionText, 56, headerSpacing + n * optionSpacing,
                if (n == selection) GameScreen.fontColorBold else GameScreen.fontColor)
        }
    }

    override fun drawBackground() {
        super.drawBackground()
        if (selection >= 0) {
            drawSelectionBox(56, headerSpacing + selection * optionSpacing, width - 112, optionSpacing)
        }
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

    override fun mouseMovedTo(screenX: Int, screenY: Int) {
        val hoverOption = mouseToOption(screenX, screenY)
        if (hoverOption >= 0) {
            selection = hoverOption
        } else {
            selection = -1
        }
    }

    override fun mouseClicked(screenX: Int, screenY: Int) {
        if (mouseToOption(screenX, screenY) >= 0) {
            selectCurrentOption()
        }
    }

    private fun selectCurrentOption() {
        if (selection >= 0 && selection < options.keys.size) {
            dismiss()
            options[options.keys.toList()[selection]]?.invoke()
        }
    }

    private fun mouseToOption(screenX: Int, screenY: Int): Int {
        val localX = screenX - x
        val localY = screenY - y
        if (localX in 1 until width) {
            val hoverOption = (localY - headerSpacing) / optionSpacing
            if (hoverOption >= 0 && hoverOption < options.keys.size) {
                return hoverOption
            }
        }
        return -1
    }
}
