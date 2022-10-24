package ui.modals

open class SplashModal(private val text: String) : Modal(260, 60, null, Position.CENTER_LOW) {

    private val padding = 24

    override fun onResize(width: Int, height: Int) {
        this.width = measure(text) + padding * 2
        this.height = padding * 2 + 20
        this.x = (width - this.width) / 2
        this.y = (height - this.height) / 2
    }
    override fun drawModalText() {
        drawString(text, padding, padding)
    }

}
