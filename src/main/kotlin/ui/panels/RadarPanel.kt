package ui.panels

import App
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import render.Screen
import render.batches.QuadBatch
import render.tilesets.Glyph
import ui.input.Mouse
import util.forXY
import util.log
import world.gen.Metamap
import world.level.CHUNK_SIZE
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
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST)
        Gdx.gl.glScissor(x + 5, yMargin + 5, width - 10, height - 10)
        drawTerrain()
        mapBatch.draw()
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST)
    }

    private fun drawTerrain() {
        val tileSize = 27
        val cx = Metamap.chunkXtoX(App.player.xy.x)
        val cy = Metamap.chunkYtoY(App.player.xy.y)
        val offx = (App.player.xy.x - Metamap.xToChunkX(cx) - CHUNK_SIZE/2).toFloat() / CHUNK_SIZE.toFloat()
        val offy = (App.player.xy.y - Metamap.yToChunkY(cy) - CHUNK_SIZE/2).toFloat() / CHUNK_SIZE.toFloat()

        forXY(0,0, 8,8) { ix,iy ->
            val meta = Metamap.metaAt(cx + ix - 4, cy + iy - 4)
            val screenX = this.x + ix * tileSize + 5 - (offx * tileSize).toInt() - tileSize
            val screenY = this.y + iy * tileSize + 5 - (offy * tileSize).toInt() - tileSize
            meta.mapIcons.forEach { mapIcon ->
                mapBatch.addPixelQuad(screenX, screenY, screenX + tileSize, screenY + tileSize,
                    mapBatch.getTextureIndex(mapIcon))
            }

        }
        val playerX = this.x + 3 * tileSize + 5
        val playerY = this.y + 3 * tileSize + 5
        mapBatch.addPixelQuad(playerX, playerY, playerX + tileSize, playerY + tileSize,
            mapBatch.getTextureIndex(Glyph.MAP_PLAYER))
    }

    override fun mouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        if (screenX in x..x+width && screenY in y..y+height) {
            App.openMap()
            return true
        }
        return false
    }
}
