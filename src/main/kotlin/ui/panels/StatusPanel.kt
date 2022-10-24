package ui.panels

object StatusPanel : ShadedPanel() {

    private val padding = 12

    init {
        this.width = 200
        this.height = 78
    }

    override fun onResize(width: Int, height: Int) {
        super.onResize(width, height)
        x = width - (this.width) - xMargin
        y = yMargin
    }

    override fun drawText() {
        drawString(App.level.statusText(), padding, padding)
        drawString(App.timeString, padding, padding + 20)
        drawString(App.dateString, padding, padding + 40)
    }
}
