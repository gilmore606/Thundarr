package ui.panels

import com.badlogic.gdx.graphics.g2d.GlyphLayout
import render.Screen
import render.tilesets.Glyph

object StatusPanel : ShadedPanel() {

    private val padding = 12

    init {
        this.width = RIGHT_PANEL_WIDTH
        this.height = 140
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

        drawString("hp", padding, padding + 70, Screen.fontColorDull, Screen.smallFont)
        drawString(App.player.hp.toString() + "/" + App.player.hpMax.toString(), padding + 20, padding + 70)
        var xc = padding
        var yc = padding + 96
        App.player.statuses.forEach { status ->
            val tag = status.panelTag()
            drawString(tag, xc, yc, status.panelTagColor(), Screen.smallFont)
            xc += GlyphLayout(Screen.smallFont, tag).width.toInt() + 10
            if (xc > width - padding) {
                xc = padding
                yc += 18
            }
        }
    }

    override fun drawBackground() {
        super.drawBackground()
        if (App.player.willAggro) {
            Screen.uiBatch.addPixelQuad(x - 46, y + 70, x - 46 + 31, y + 70 + 31,
                Screen.uiBatch.getTextureIndex(Glyph.ANGRY_THUNDARR))
        }
    }

    override fun drawEntities() {
        Screen.uiBatch.addHealthBar(x + padding + 75, y + padding + 71,
            x + width - padding * 2 + 2, y + padding + 71 + 12,
            App.player.hp, App.player.hpMax)
    }
}
