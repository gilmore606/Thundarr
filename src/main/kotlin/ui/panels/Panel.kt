package ui.panels

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import render.Screen
import render.batches.QuadBatch
import render.tilesets.Glyph
import ui.input.Mouse
import java.lang.Integer.max

abstract class Panel {

    companion object {
        const val RIGHT_PANEL_WIDTH = 200
    }

    var x = 0
    var y = 0
    var width = 500
    var height = 500

    protected var shadowOffset = 10
    protected var borderWidth = 2

    var dismissed = false

    // Amount of space to leave at window edges, scaled by window size.
    protected var xMargin = 16
    protected var yMargin = 16

    protected open fun myTextBatch() = Screen.textBatch
    protected open fun myBoxBatch() = Screen.uiBatch
    protected open fun myThingBatch(): QuadBatch? = Screen.thingBatch
    protected open fun myActorBatch(): QuadBatch? = Screen.actorBatch

    open fun onResize(width: Int, height: Int) {
        xMargin = 2 + (max(0, width - 800) / 70)
        yMargin = 2 + (max(0, height - 400) / 60)
    }


    open fun onRender(delta: Float) { }

    fun renderText() {
        this.drawText()
    }

    fun renderBackground() {
        this.drawBackground()
    }

    fun renderEntities() {
        this.drawEntities()
    }

    open fun drawsGrouped() = true
    open fun drawsSeparate() = false
    open fun drawEverything() { }
    open fun drawBackground() { }
    open fun drawEntities() { }
    open fun drawActors() { }

    open fun mouseMovedTo(screenX: Int, screenY: Int) { }
    open fun mouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean { return false }

    abstract fun drawText()

    protected fun measure(text: String, font: BitmapFont = Screen.font) = GlyphLayout(font, text).width.toInt()

    fun drawString(text: String, x: Int, y: Int, color: Color = Screen.fontColor, font: BitmapFont = Screen.font) {
        font.color = color
        font.draw(myTextBatch(), text, ((x  + this.x) - (Screen.width / 2f)), 0f - ((y + this.y) - (Screen.height / 2f)))
    }

    fun drawStringAbsolute(text: String, x: Int, y: Int, color: Color = Screen.fontColor, font: BitmapFont = Screen.font) {
        font.color = color
        font.draw(myTextBatch(), text, (x - Screen.width / 2f), 0f - (y - Screen.height / 2f))
    }

    fun drawTitle(text: String) {
        val xOffset = (width - GlyphLayout(Screen.titleFont, text).width) / 2f
        val yOffset = 24f
        Screen.titleFont.draw(myTextBatch(), text, ((this.x + xOffset) - (Screen.width / 2f)), 0f - ((this.y + yOffset) - (Screen.height / 2f)))
    }

    fun drawSubTitle(text: String) {
        val xOffset = (width - GlyphLayout(Screen.subTitleFont, text).width) / 2f
        val yOffset = 52f
        Screen.subTitleFont.draw(myTextBatch(), text, ((this.x + xOffset) - (Screen.width / 2f)), 0f - ((this.y + yOffset) - (Screen.height / 2f)))
    }

    fun drawBox(x: Int, y: Int, width: Int, height: Int) {
        myBoxBatch().addPixelQuad(x+ shadowOffset, y + (shadowOffset * 1.2).toInt(), x + width + shadowOffset, y + height + shadowOffset,
            myBoxBatch().getTextureIndex(Glyph.BOX_SHADOW))
        myBoxBatch().addPixelQuad(x - borderWidth, y - borderWidth, x + width + borderWidth, y + height + borderWidth,
            myBoxBatch().getTextureIndex(Glyph.BOX_BORDER))
        myBoxBatch().addPixelQuad(x, y, x + width, y + height,
            myBoxBatch().getTextureIndex(Glyph.BOX_BG))
    }

    fun drawShade(x: Int, y: Int, width: Int, height: Int) {
        myBoxBatch().addPixelQuad(x, y, x + width, y + height,
            myBoxBatch().getTextureIndex(Glyph.WINDOW_SHADE))
    }

    fun drawQuad(x: Int, y: Int, width: Int, height: Int, glyph: Glyph, alpha: Float = 1f) {
        myBoxBatch().addPixelQuad(x + this.x, y + this.y, x + width + this.x, y + height + this.y,
            myBoxBatch().getTextureIndex(glyph), alpha = alpha)
    }

    fun drawWrappedText(text: List<String>, x0: Int, y0: Int, spacing: Int = 20,
                                  font: BitmapFont = Screen.smallFont,
                                  color: Color = Screen.fontColor) {
        text.forEachIndexed { n, line ->
            drawString(line, x0, y0 + n * spacing, color, font)
        }
    }

    fun drawRightText(text: String, x0: Int, y0: Int, color: Color = Screen.fontColor, font: BitmapFont = Screen.font) {
        drawString(text, x0 - GlyphLayout(font, text).width.toInt(), y0, color, font)
    }

    fun drawCenterText(text: String, x0: Int, y0: Int, width: Int, color: Color = Screen.fontColor, font: BitmapFont = Screen.font) {
        drawString(text, x0 + (width - GlyphLayout(font, text).width.toInt()) / 2, y0, color, font)
    }

    open fun dispose() { }

    fun isInBounds(screenX: Int, screenY: Int) = !(screenX < this.x || screenX > (this.x + width) || screenY < this.y || screenY > this.y + height)
}
