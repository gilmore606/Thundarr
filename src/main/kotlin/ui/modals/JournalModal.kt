package ui.modals

import render.Screen
import render.batches.QuadBatch
import ui.input.Keydef
import ui.input.Mouse

class JournalModal : Modal(600, 600, "- yOUr dEEDs -") {

    companion object {
        val wrapWidth = 600
        val wrapPadding = 24
    }
    override fun newThingBatch() = null
    override fun newActorBatch() = null

    val padding = wrapPadding

    override fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        if (super.onMouseClicked(screenX, screenY, button)) return true
        dismiss()
        return true
    }

    override fun onKeyDown(key: Keydef) {
        when (key) {
            Keydef.OPEN_JOURNAL, Keydef.CANCEL -> dismiss()
            Keydef.OPEN_INV -> replaceWith(ThingsModal(App.player))
            Keydef.OPEN_SKILLS -> replaceWith(SkillsModal(App.player))
            Keydef.OPEN_GEAR -> replaceWith(GearModal(App.player))
            Keydef.OPEN_MAP -> replaceWith(MapModal())
            else -> super.onKeyDown(key)
        }
    }

    override fun drawModalText() {
        super.drawModalText()
        var yc = padding + 80
        var xc = padding
        App.player.journal.entries.forEach { entry ->
            drawString(entry.gameTime.dateString, xc, yc, Screen.fontColorDull)
            yc += 24
            drawString(entry.title(), xc, yc, Screen.fontColorBold, Screen.subTitleFont)
            yc += 30
            entry.wrapped.forEach { line ->
                drawString(line, xc, yc, Screen.fontColorBold, Screen.smallFont)
                yc += 20
            }
            yc += 20
        }
    }
}
