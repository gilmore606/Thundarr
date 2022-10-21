package ui.panels

import render.GameScreen

object LookPanel : ShadedPanel() {

    private val padding = 12

    init {
        this.width = 150
        this.height = 180
    }

    override fun onResize(width: Int, height: Int) {
        x = width - (this.width + padding * 2)
        y = padding + if (GameScreen.panels.contains(StatusPanel)) (StatusPanel.height + padding) else 0
    }

    override fun drawText() {
        if (GameScreen.cursorPosition != null) {

        }
    }

    override fun drawBackground() {
        if (GameScreen.cursorPosition != null) {
            super.drawBackground()
        }
    }
}
