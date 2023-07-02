package ui.panels

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import render.Screen
import render.batches.QuadBatch
import render.tilesets.Glyph
import util.log
import world.gen.Metamap
import world.level.WorldLevel

object RadarPanel : ShadedPanel() {

    private val mapBatch = QuadBatch(Screen.mapTileSet, maxQuads = 300)

    init {
        this.width = RIGHT_PANEL_WIDTH
        this.height = RIGHT_PANEL_WIDTH
    }

    override fun drawsSeparate() = true

    override fun onResize(width: Int, height: Int) {
        super.onResize(width, height)
        x = width - (this.width) - xMargin
        y = height - (this.height) - yMargin
    }

    private fun shouldDraw() = Screen.showRadar && (App.player.level is WorldLevel)

    override fun drawBackground() {
        if (!shouldDraw()) return
        super.drawBackground()
    }

    override fun drawEverything() {
        if (!shouldDraw()) return
        mapBatch.clear()
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        drawTerrain()
        mapBatch.draw()
    }

    private fun drawTerrain() {
        val tileSize = 27
        val cx = Metamap.chunkXtoX(App.player.xy.x)
        val cy = Metamap.chunkYtoY(App.player.xy.y)
        for (ix in 0..6) {
            for (iy in 0 .. 6) {
                val meta = Metamap.metaAt(cx + ix - 3, cy + iy - 3)
                val screenX = this.x + ix * tileSize + 5
                val screenY = this.y + iy * tileSize + 5
                meta.mapIcons.forEach { mapIcon ->
                    mapBatch.addPixelQuad(screenX, screenY, screenX + tileSize, screenY + tileSize,
                        mapBatch.getTextureIndex(mapIcon))
                }
                if (ix == 3 && iy == 3) {
                    mapBatch.addPixelQuad(screenX, screenY, screenX + tileSize, screenY + tileSize,
                        mapBatch.getTextureIndex(Glyph.MAP_PLAYER))
                }
            }
        }
    }

}
