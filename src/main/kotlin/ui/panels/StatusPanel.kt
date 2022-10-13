package ui.panels

import App

object StatusPanel : Panel() {

    private val padding = 12

    init {
        this.width = 100
        this.height = 100
    }

    override fun onResize(width: Int, height: Int) {
        x = width - this.width - padding
        y = padding
    }

    override fun drawBackground() { }

    override fun drawText() {
        drawString("x: ${App.player.xy.x} y: ${App.player.xy.y}", 0, 0)
        drawString("turn ${App.turnTime.toInt()}", 0, 20)
    }

}
