package ui

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import render.GameScreen
import render.QuadBatch
import util.Glyph

abstract class Panel {

    private lateinit var font: BitmapFont
    protected var fontSize: Int = 14
    private lateinit var textBatch: SpriteBatch
    private lateinit var boxBatch: QuadBatch

    protected var x = 0
    protected var y = 0
    protected var width = 500
    protected var height = 500

    private val shadowOffset = 8
    private val borderWidth = 2

    open fun onResize(width: Int, height: Int) { }

    fun renderText(font: BitmapFont, fontSize: Int, batch: SpriteBatch) {
        this.font = font
        this.fontSize = fontSize
        this.textBatch = batch
        this.drawText()
    }

    fun renderBackground(batch: QuadBatch) {
        this.boxBatch = batch
        this.drawBackground()
    }

    abstract fun drawBackground()

    abstract fun drawText()

    protected fun drawString(text: String, x: Int, y: Int, isBold: Boolean = false) {
        font.setColor(if (isBold) GameScreen.fontColorBold else GameScreen.fontColor)
        font.draw(textBatch, text, ((x  + this.x) - (GameScreen.width / 2f)), 0f - ((y + this.y) - (GameScreen.height / 2f)))
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
