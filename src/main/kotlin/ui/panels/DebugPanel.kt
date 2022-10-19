package ui.panels

import App
import render.GameScreen

object DebugPanel : Panel() {

    private val padding = 12

    init {
        this.width = 130
        this.height = 100
    }

    override fun onResize(width: Int, height: Int) {
        x = padding
        y = padding
    }

    override fun drawText() {
        drawString("x: ${App.player.xy.x} y: ${App.player.xy.y}", 0, 0)
        drawString(App.level.debugText(), 0, 20)

        val l = App.level.lightAt(App.player.xy.x, App.player.xy.y)
        val lr = (l.r * 100).toInt()
        val lg = (l.g * 100).toInt()
        val lb = (l.b * 100).toInt()
        drawString("light $lr $lg $lb", 0, 40)
        drawString("render ${GameScreen.drawTime} ms", 0, 60)
    }

}
