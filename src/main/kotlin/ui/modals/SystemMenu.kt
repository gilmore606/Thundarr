package ui.modals

import render.batches.QuadBatch
import render.tilesets.UITileSet

class SystemMenu : SelectionModal(270, 200, "- ThUNdARR -", Position.LEFT) {

    companion object {
        val boxBatch = QuadBatch(UITileSet())
    }

    override fun newBoxBatch() = SystemMenu.boxBatch
    override fun newThingBatch() = null
    override fun newActorBatch() = null

    private val options = LinkedHashMap<String, ()->Unit>().apply {
        put("Resume") { }
        put("Settings") { App.openSettings() }
        put("Controls") { App.openControls() }
        put("Save and exit") { App.saveAndReturnToMenu() }
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

    override fun dispose() {
        textBatch.dispose()
    }
}
