package ui.panels

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import render.Screen
import render.tilesets.Glyph
import ui.modals.StatusSidecar
import util.log

object StatusPanel : ShadedPanel() {

    private val padding = 12

    private val sidecar = StatusSidecar(this)

    private var tagStrs = App.player.statuses.map { it.panelTag() }
    private var tagColors = App.player.statuses.map { it.panelTagColor() }
    private var tagWidths = tagStrs.map { GlyphLayout(Screen.smallFont, it).width.toInt() }
    private var tagXs = ArrayList<Int>()
    private var tagYs = ArrayList<Int>()
    private var statuses = App.player.statuses

    init {
        this.width = RIGHT_PANEL_WIDTH
        this.height = 140
        refillCache()
    }

    fun refillCache() {
        var cursor = padding
        var cursorY = 100 + padding
        statuses = App.player.statuses
        tagStrs = App.player.statuses.map { it.panelTag() }
        tagColors = App.player.statuses.map { it.panelTagColor() }
        tagWidths = tagStrs.map { GlyphLayout(Screen.smallFont, it).width.toInt() }
        tagXs.clear()
        tagYs.clear()
        tagStrs.forEachIndexed { n, tagStr ->
            if (cursor + tagWidths[n] < (width - padding)) {
                tagXs.add(n, cursor)
                tagYs.add(n, cursorY)
                cursor += tagWidths[n] + 10
            } else {
                tagXs.add(n, padding)
                cursor = padding + tagWidths[n] + 10
                cursorY += 18
                tagYs.add(n, cursorY)
            }
        }
    }

    override fun onResize(width: Int, height: Int) {
        super.onResize(width, height)
        x = width - (this.width) - xMargin
        y = yMargin
        sidecar.onResize(width, height)
    }

    override fun drawText() {
        drawString(App.level.statusText(), padding, padding)
        drawString(App.timeString, padding, padding + 25, Screen.fontColorDull, Screen.smallFont)
        drawString(App.dateString, padding, padding + 45, Screen.fontColorDull, Screen.smallFont)

        drawString("hp", padding, padding + 70, Screen.fontColorDull, Screen.smallFont)
        drawString(App.player.hp.toString() + "/" + App.player.hpMax.toString(), padding + 20, padding + 70)
        for (i in 0 .. tagStrs.lastIndex) {
            drawString(tagStrs[i], tagXs[i], tagYs[i], tagColors[i], Screen.smallFont)
        }
    }

    override fun mouseMovedTo(screenX: Int, screenY: Int) {
        val lx = screenX - x
        val ly = screenY - y
        var shown = false
        for (i in 0..tagStrs.lastIndex) {
            if (lx >= tagXs[i] - 3 && lx <= tagXs[i] + tagWidths[i] + 6 &&
                        ly >= tagYs[i] - 3 && ly <= tagYs[i] + 20) {
                sidecar.showStatus(statuses[i])
                shown = true
            }
        }
        if (!shown) sidecar.showStatus(null)
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

    override fun drawsSeparate() = true
    override fun drawEverything() {
        if (sidecar.status == null) return
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glEnable(GL20.GL_BLEND)

        sidecar.clearBoxBatch()
        sidecar.renderBackground()
        sidecar.drawBoxBatch()
        sidecar.beginTextBatch()
        sidecar.renderText()
        sidecar.endTextBatch()
    }
}
