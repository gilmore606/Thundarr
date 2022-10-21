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

    init {
        maxSelection = options.size - 1
        spacing = 30
        padding = 36
        height = spacing * options.size + headerPad + padding / 2
    }

    override fun drawModalText() {
        options.keys.forEachIndexed { n, optionText ->
            drawOptionText(optionText, n)
        }
    }

    override fun drawBackground() {
        super.drawBackground()
        drawOptionShade()
    }

    override fun doSelect() {
        if (selection >= 0 && selection < options.keys.size) {
            dismiss()
            options[options.keys.toList()[selection]]?.invoke()
        }
    }

}
