package ui.modals

import com.badlogic.gdx.Input
import render.GameScreen
import ui.input.Mouse

class EscMenu : SelectionModal(270, 230, "- ThUNdARR -") {

    private val options = LinkedHashMap<String, ()->Unit>().apply {
        put("Restart world") { App.restartWorld() }
        put("Settings") { App.openSettings() }
        put("Controls") { App.openControls() }
        put("Credits") { App.openCredits() }
        put("Save and quit") { App.saveAndQuit() }
    }
    private val optionSpacing = GameScreen.fontSize + 12
    private val headerSpacing = 72

    override fun drawModalText() {
        options.keys.forEachIndexed { n, optionText ->
            drawString(optionText, 56, headerSpacing + n * optionSpacing,
                if (n == selection) GameScreen.fontColorBold else GameScreen.fontColor)
        }
    }

    override fun drawBackground() {
        super.drawBackground()
        if (!isAnimating() && selection >= 0) {
            drawSelectionBox(56, headerSpacing + selection * optionSpacing, width - 112, optionSpacing)
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

    override fun mouseClicked(screenX: Int, screenY: Int, button: Mouse.Button) {
        if (mouseToOption(screenX, screenY) >= 0) {
            doSelect()
        }
    }

    override fun doSelect() {
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
