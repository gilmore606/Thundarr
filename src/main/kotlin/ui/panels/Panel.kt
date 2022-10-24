package ui.panels

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import render.GameScreen
import render.QuadBatch
import render.tilesets.Glyph
import ui.input.Mouse

abstract class Panel {

    private lateinit var textBatch: SpriteBatch
    protected lateinit var boxBatch: QuadBatch
    protected lateinit var thingBatch: QuadBatch
    protected lateinit var actorBatch: QuadBatch

    var x = 0
    var y = 0
    var width = 500
    var height = 500

    protected var shadowOffset = 10
    protected var borderWidth = 2

    open fun onResize(width: Int, height: Int) { }

    open fun onRender(delta: Float) { }

    fun renderText(batch: SpriteBatch) {
        this.textBatch = batch
        this.drawText()
    }

    fun renderBackground(batch: QuadBatch) {
        this.boxBatch = batch
        this.drawBackground()
    }

    fun renderThings(batch: QuadBatch) {
        this.thingBatch = batch
        this.drawThings()
    }

    fun renderActors(batch: QuadBatch) {
        this.actorBatch = batch
        this.drawActors()
    }

    open fun drawBackground() { }
    open fun drawThings() { }
    open fun drawActors() { }

    open fun mouseMovedTo(screenX: Int, screenY: Int) { }
    open fun mouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean { return false }

    abstract fun drawText()

    protected fun drawString(text: String, x: Int, y: Int, color: Color = GameScreen.fontColor, font: BitmapFont = GameScreen.font) {
        font.color = color
        font.draw(textBatch, text, ((x  + this.x) - (GameScreen.width / 2f)), 0f - ((y + this.y) - (GameScreen.height / 2f)))
    }

    protected fun drawTitle(text: String) {
        val xOffset = (width - GlyphLayout(GameScreen.titleFont, text).width) / 2f
        val yOffset = 24f
        GameScreen.titleFont.draw(textBatch, text, ((this.x + xOffset) - (GameScreen.width / 2f)), 0f - ((this.y + yOffset) - (GameScreen.height / 2f)))
    }

    protected fun drawBox(x: Int, y: Int, width: Int, height: Int) {
        boxBatch.addPixelQuad(x+ shadowOffset, y + (shadowOffset * 1.2).toInt(), x + width + shadowOffset, y + height + shadowOffset,
            boxBatch.getTextureIndex(Glyph.BOX_SHADOW))
        boxBatch.addPixelQuad(x - borderWidth, y - borderWidth, x + width + borderWidth, y + height + borderWidth,
            boxBatch.getTextureIndex(Glyph.BOX_BORDER))
        boxBatch.addPixelQuad(x, y, x + width, y + height,
            boxBatch.getTextureIndex(Glyph.BOX_BG))
    }
}
