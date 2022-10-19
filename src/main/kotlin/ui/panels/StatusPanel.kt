package ui.panels

object StatusPanel : Panel() {

    private val padding = 12

    init {
        this.width = 130
        this.height = 100
    }

    override fun onResize(width: Int, height: Int) {
        x = width - (this.width + padding)
        y = padding
    }

    override fun drawText() {
        drawString(App.level.statusText(), 0, 0)
        drawString(App.timeString, 0, 20)
        drawString(App.dateString, 0, 40)
    }
}
