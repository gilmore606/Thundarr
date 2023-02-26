package ui.modals

open class SplashModal(private val text: String, val forceHeight: Int? = null) :
    Modal(260, forceHeight ?: 60, null, Position.CENTER_LOW) {

    private val padding = 24
    override fun openSound() = null
    override fun closeSound() = null

    override fun onResize(width: Int, height: Int) {
        this.width = measure(text) + padding * 2
        this.height = forceHeight ?: (padding * 2 + 20)
        this.x = (width - this.width) / 2
        this.y = (height - this.height) / 2
    }
    override fun drawModalText() {
        drawString(text, padding, padding)
    }

}
