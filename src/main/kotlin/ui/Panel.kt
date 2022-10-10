package ui

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import util.log

abstract class Panel {

    private lateinit var font: BitmapFont
    private lateinit var batch: SpriteBatch

    protected var screenWidth = 100
    protected var screenHeight = 100

    protected var x = 0
    protected var y = 0
    protected var width = 500
    protected var height = 500

    open fun onResize(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
    }

    fun renderText(font: BitmapFont, batch: SpriteBatch) {
        this.font = font
        this.batch = batch
        this.drawText()
    }

    abstract fun drawText()

    protected fun drawString(text: String, x: Int, y: Int) {
        font.draw(batch, text, ((x  + this.x) - (screenWidth / 2f)), 0f - ((y + this.y) - (screenHeight / 2f)))
    }
}
