package ui.panels

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import render.Screen
import render.QuadBatch
import render.tilesets.Glyph
import ui.input.Mouse
import java.lang.Integer.max

abstract class Panel {

    companion object {
        const val RIGHT_PANEL_WIDTH = 200
    }

    private lateinit var textBatch: SpriteBatch
    protected lateinit var boxBatch: QuadBatch

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

    open fun onResize(width: Int, height: Int) {
        xMargin = 2 + (max(0, width - 800) / 70)
        yMargin = 2 + (max(0, height - 400) / 60)
    }


    open fun onRender(delta: Float) { }

    fun renderText(batch: SpriteBatch) {
        this.textBatch = batch
        this.drawText()
    }

    fun renderBackground(batch: QuadBatch) {
        this.boxBatch = batch
        this.drawBackground()
    }

    fun renderEntities() {
        this.drawEntities()
    }

    open fun drawBackground() { }
    open fun drawEntities() { }
    open fun drawActors() { }

    open fun mouseMovedTo(screenX: Int, screenY: Int) { }
    open fun mouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean { return false }

    abstract fun drawText()

    protected fun measure(text: String) = GlyphLayout(Screen.font, text).width.toInt()

    protected fun drawString(text: String, x: Int, y: Int, color: Color = Screen.fontColor, font: BitmapFont = Screen.font) {
        font.color = color
        font.draw(textBatch, text, ((x  + this.x) - (Screen.width / 2f)), 0f - ((y + this.y) - (Screen.height / 2f)))
    }

    protected fun drawTitle(text: String) {
        val xOffset = (width - GlyphLayout(Screen.titleFont, text).width) / 2f
        val yOffset = 24f
        Screen.titleFont.draw(textBatch, text, ((this.x + xOffset) - (Screen.width / 2f)), 0f - ((this.y + yOffset) - (Screen.height / 2f)))
    }

    protected fun drawBox(x: Int, y: Int, width: Int, height: Int) {
        boxBatch.addPixelQuad(x+ shadowOffset, y + (shadowOffset * 1.2).toInt(), x + width + shadowOffset, y + height + shadowOffset,
            boxBatch.getTextureIndex(Glyph.BOX_SHADOW))
        boxBatch.addPixelQuad(x - borderWidth, y - borderWidth, x + width + borderWidth, y + height + borderWidth,
            boxBatch.getTextureIndex(Glyph.BOX_BORDER))
        boxBatch.addPixelQuad(x, y, x + width, y + height,
            boxBatch.getTextureIndex(Glyph.BOX_BG))
    }

    protected fun drawQuad(x: Int, y: Int, width: Int, height: Int, glyph: Glyph) {
        boxBatch.addPixelQuad(x + this.x, y + this.y, x + width + this.x, y + height + this.y,
            boxBatch.getTextureIndex(glyph))
    }

    protected fun wrapText(text: String, width: Int, padding: Int, font: BitmapFont = Screen.smallFont): ArrayList<String> {
        val wrapped = ArrayList<String>()
        var remaining = text
        var nextLine = ""
        var linePixelsLeft = (width - padding * 2)
        val spaceWidth = GlyphLayout(font, " ").width.toInt()
        while (remaining.isNotEmpty() || remaining == " ") {
            // get next word
            val space = remaining.indexOf(' ')
            var word = ""
            if (space >= 0) {
                word = remaining.substring(0, space)
                remaining = remaining.substring(space + 1, remaining.length)
            } else {
                word = remaining
                remaining = ""
            }
            if (word != " ") {
                val wordWidth = GlyphLayout(font, word).width.toInt()
                if (nextLine == "" || wordWidth <= linePixelsLeft) {
                    nextLine += word + " "
                    linePixelsLeft -= wordWidth + spaceWidth
                } else {
                    wrapped.add(nextLine)
                    nextLine = word + " "
                    linePixelsLeft = (width - padding * 2) - wordWidth - spaceWidth
                }
            }
        }
        if (nextLine != "") wrapped.add(nextLine)
        return wrapped
    }

    protected fun drawWrappedText(text: List<String>, x0: Int, y0: Int, spacing: Int = 20,
                                  font: BitmapFont = Screen.smallFont,
                                  color: Color = Screen.fontColor) {
        text.forEachIndexed { n, line ->
            drawString(line, x0, y0 + n * spacing, color, font)
        }
    }
}
