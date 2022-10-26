package ui.panels

import render.Screen

object StatusPanel : ShadedPanel() {

    private val padding = 12

    init {
        this.width = 200
        this.height = 120
    }

    override fun onResize(width: Int, height: Int) {
        super.onResize(width, height)
        x = width - (this.width) - xMargin
        y = yMargin
    }

    override fun drawText() {
        drawString(App.level.statusText(), padding, padding)
        drawString(App.timeString, padding, padding + 25, Screen.fontColorDull, Screen.smallFont)
        drawString(App.dateString, padding, padding + 45, Screen.fontColorDull, Screen.smallFont)

        drawString(App.player.hp.toString() + "/" + App.player.hpMax.toString(), padding, padding + 70)
    }

    override fun drawEntities() {
        Screen.uiBatch.addHealthBar(x + padding + 55, y + padding + 71,
            x + width - padding * 2 + 2, y + padding + 71 + 12,
            App.player.hp, App.player.hpMax)
    }
}
