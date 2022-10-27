package ui.panels

import App
import render.Screen
import world.LevelKeeper

object DebugPanel : ShadedPanel() {

    private val padding = 12

    init {
        this.width = 160
        this.height = 130
    }

    override fun onResize(width: Int, height: Int) {
        super.onResize(width, height)
        x = xMargin
        y = yMargin
    }

    override fun drawText() {
        drawString("x: ${App.player.xy.x} y: ${App.player.xy.y}", padding, padding)
        drawString(App.level.debugText(), padding, padding + 20)

        val l = App.level.lightAt(App.player.xy.x, App.player.xy.y)
        val lr = (l.r * 100).toInt()
        val lg = (l.g * 100).toInt()
        val lb = (l.b * 100).toInt()
        drawString("light $lr $lg $lb", padding, padding + 40)
        drawString("render ${Screen.drawTime} ms", padding, padding + 60)
        drawString("${LevelKeeper.liveLevels.size} live levels", padding, padding + 80)

    }

}
