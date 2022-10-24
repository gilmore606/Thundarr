package ui.panels

import render.GameScreen

object StatusPanel : ShadedPanel() {

    private val padding = 12

    init {
        this.width = 200
        this.height = 83
    }

    override fun onResize(width: Int, height: Int) {
        super.onResize(width, height)
        x = width - (this.width) - xMargin
        y = yMargin
    }

    override fun drawText() {
        drawString(App.level.statusText(), padding, padding)
        drawString(App.timeString, padding, padding + 25, GameScreen.fontColorDull, GameScreen.smallFont)
        drawString(App.dateString, padding, padding + 45, GameScreen.fontColorDull, GameScreen.smallFont)
    }
}
