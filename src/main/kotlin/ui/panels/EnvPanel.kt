package ui.panels

import render.Screen
import world.gen.Metamap
import world.level.EnclosedLevel
import world.level.WorldLevel

object EnvPanel : ShadedPanel() {

    private val padding = 12

    init {
        this.width = RIGHT_PANEL_WIDTH
        this.height = 130
    }

    override fun onResize(width: Int, height: Int) {
        super.onResize(width, height)
        x = xMargin
        y = yMargin
    }

    override fun drawText() {
        val threat = App.player.threatLevel() - App.player.xpLevel
        val threatDesc = if (threat < 0) "relaxed"
            else if (threat == 0) "cautious"
            else if (threat == 1) "anxious"
            else "dreadful"

        drawString(App.level.statusText(), padding, padding, Screen.fontColorBold, Screen.smallFont)

        drawString(App.gameTime.timeString, padding, padding + 28, Screen.fontColor, Screen.smallFont)

        drawString(App.gameTime.dateString, padding, padding + 50, Screen.fontColorDull, Screen.smallFont)

        drawString("cool, windy", padding, padding + 72, Screen.fontColorDull, Screen.smallFont)

        drawString(threatDesc, padding, padding + 94, Screen.fontColorDull, Screen.smallFont)
    }
}
