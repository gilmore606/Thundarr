package ui.modals

import render.batches.QuadBatch

class ControlsModal : Modal(330, 360, "- debug controls -") {

    override fun newThingBatch() = null
    override fun newActorBatch() = null

    override fun drawModalText() {
        drawString("F1 : toggle debug vision", 25, 80)
        drawString("F2 : place lightbulb", 25, 107)
        drawString("F3 : remove object", 25, 134)
        drawString("F4 : place tree", 25, 160)
        drawString("F5 : debug panel", 25, 186)
        drawString("numpad : walk around", 25, 220)
        drawString("num 5  : interact / select", 25, 240)
        drawString("num /  : enter/exit cursor mode", 25, 260)
        drawString("-/= : lower/raise zoom", 25, 280)
        drawString("Click and drag to move the view!", 25, 320)
    }

}
