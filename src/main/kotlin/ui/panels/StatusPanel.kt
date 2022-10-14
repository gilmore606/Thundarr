package ui.panels

import App

object StatusPanel : Panel() {

    private val padding = 12

    init {
        this.width = 130
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
        drawString(App.level.debugText(), 0, 40)
        val l = App.level.lightAt(App.player.xy.x, App.player.xy.y)
        val lr = (l.r * 100).toInt()
        val lg = (l.g * 100).toInt()
        val lb = (l.b * 100).toInt()
        drawString("light $lr $lg $lb", 0, 60)
    }

}
