package ui.modals

import App
import render.Screen

class AttractMenu : SelectionModal(270, 230, "- ThUNdARR -", Position.LEFT) {

    private val options = LinkedHashMap<String, ()->Unit>()

    init {
        spacing = 32
        padding = 48
        headerPad += 48
        dismissible = false
    }

    fun populate() {
        options.clear()
        if (App.save.worldExists()) {
            options.put("Continue") { App.doContinue() }
        }
        options.put("Start new game") { App.doStartNewGame() }
        options.put("Settings") { App.openSettings() }
        options.put("Controls") { App.openControls() }
        options.put("Credits") { App.openCredits() }
        options.put("Quit") { App.doQuit() }

        maxSelection = options.size - 1
        height = spacing * options.size + headerPad + padding / 2
    }

    override fun drawModalText() {
        drawCenterText("the bARbARiAn", 0, 54, width, Screen.fontColor, Screen.subTitleFont)
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

    override fun dismiss() {
        if (!App.attractMode) {
            super.dismiss()
        }
    }

    fun dismissSelf() {
        dismissible = true
        dismiss()
    }

}
