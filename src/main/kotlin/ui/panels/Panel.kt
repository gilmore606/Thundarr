package ui.panels

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import render.GameScreen
import render.QuadBatch
import render.tilesets.Glyph

abstract class Panel {

    private lateinit var textBatch: SpriteBatch
    protected lateinit var boxBatch: QuadBatch

    protected var x = 0
    protected var y = 0
    protected var width = 500
    protected var height = 500

    private val shadowOffset = 8
    private val borderWidth = 2

    open fun onResize(width: Int, height: Int) { }

    fun renderText(batch: SpriteBatch) {
        this.textBatch = batch
        this.drawText()
    }

    fun renderBackground(batch: QuadBatch) {
        this.boxBatch = batch
        this.drawBackground()
    }

    abstract fun drawBackground()

    abstract fun drawText()

    protected fun drawString(text: String, x: Int, y: Int, color: Color = GameScreen.fontColor) {
        GameScreen.font.color = color
        GameScreen.font.draw(textBatch, text, ((x  + this.x) - (GameScreen.width / 2f)), 0f - ((y + this.y) - (GameScreen.height / 2f)))
    }

    protected fun drawTitle(text: String) {
        val xOffset = (width - GlyphLayout(GameScreen.titleFont, text).width) / 2f
        val yOffset = 16f
        GameScreen.titleFont.draw(textBatch, text, ((this.x + xOffset) - (GameScreen.width / 2f)), 0f - ((this.y + yOffset) - (GameScreen.height / 2f)))
    }

    protected fun drawBox(x: Int, y: Int, width: Int, height: Int) {
        boxBatch.addPixelQuad(x+ shadowOffset, y + shadowOffset, x + width + shadowOffset, y + height + shadowOffset,
            boxBatch.getTextureIndex(Glyph.BOX_SHADOW))
        boxBatch.addPixelQuad(x - borderWidth, y - borderWidth, x + width + borderWidth, y + height + borderWidth,
            boxBatch.getTextureIndex(Glyph.BOX_BORDER))
        boxBatch.addPixelQuad(x, y, x + width, y + height,
            boxBatch.getTextureIndex(Glyph.BOX_BG))
    }
}
