package ui.panels

import render.Screen
import world.gen.Metamap
import world.level.EnclosedLevel
import world.level.WorldLevel

object EnvPanel : ShadedPanel() {

    private val padding = 12

    init {
        this.width = RIGHT_PANEL_WIDTH
        this.height = 162
    }

    override fun onResize(width: Int, height: Int) {
        super.onResize(width, height)
        x = xMargin
        y = yMargin
    }

    override fun drawBackground() {
        if (Console.mouseInside || Console.inputActive) return
        super.drawBackground()
    }
    override fun drawText() {
        if (Console.mouseInside || Console.inputActive) return
        val threat = App.player.threatLevel()
        val threatDesc = if (threat < 0) "relaxed"
            else if (threat == 0 || threat == 1) "cautious"
            else if (threat == 2 || threat == 3) "nervous"
            else "dreadful"

        drawString(App.level.statusText(), padding, padding, Screen.fontColorBold, Screen.font)

        drawString(App.gameTime.timeString, padding, padding + 26, Screen.fontColor, Screen.font)

        drawString(App.gameTime.dateString, padding, padding + 54, Screen.fontColorDull, Screen.smallFont)

        drawString(App.weather.envString, padding, padding + 76, Screen.fontColorDull, Screen.smallFont)

        val t1 = App.player.temperature
        val t2 = App.player.feltTemperature
        drawString(if (t1 == t2) "${t1}F" else "${t1}F (feels ${t2}F)",
            padding, padding + 98, Screen.fontColorDull, Screen.smallFont)

        drawString(threatDesc, padding, padding + 120, Screen.fontColorDull, Screen.smallFont)
    }
}
