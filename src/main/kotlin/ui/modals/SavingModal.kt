package ui.modals

class SavingModal : Modal(150, 60, null, Position.CENTER_LOW) {

    override fun drawModalText() {
        drawString("Saving...", 48, 24)
    }

}
