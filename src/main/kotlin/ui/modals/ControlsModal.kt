package ui.modals

class ControlsModal : Modal(450, 240, "- debug controls -") {

    override fun drawModalText() {
        drawString("F1 : toggle debug vision", 25, 80)
        drawString("F2 : place lightbulb", 25, 107)
        drawString("F3 : remove object", 25, 134)
        drawString("F4 : place tree", 25, 160)
        drawString("Click and drag to move the view!", 102, 200)
    }

}
