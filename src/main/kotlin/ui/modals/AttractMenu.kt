package ui.modals

import App
import render.Screen
import render.batches.QuadBatch
import render.tilesets.UITileSet

class AttractMenu : SelectionModal(270, 230, "- ThUNdARR -", Position.LEFT) {

    private val options = LinkedHashMap<String, ()->Unit>()
    companion object {
        val boxBatch = QuadBatch(UITileSet())
    }

    override fun newBoxBatch() = AttractMenu.boxBatch
    override fun newThingBatch() = null
    override fun newActorBatch() = null

    init {
        spacing = 32
        padding = 48
        headerPad += 48
        dismissible = false
        animTime = 200f
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

    override fun myXmargin(): Int {
        return 100
    }

    override fun drawModalText() {
        if (isAnimating()) return
        drawCenterText("the bARbARiAn", 0, 54, width, Screen.fontColor, Screen.subTitleFont)
        if ((System.currentTimeMillis() - launchTimeMs) > animTime * 2f) {
            options.keys.forEachIndexed { n, optionText ->
                drawOptionText(optionText, n)
            }
        }
    }

    override fun drawBackground() {
        super.drawBackground()
        if (!isAnimating()) drawOptionShade()
    }

    override fun doSelect() {
        super.doSelect()
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
    override fun dispose() {
        textBatch.dispose()
    }
}
