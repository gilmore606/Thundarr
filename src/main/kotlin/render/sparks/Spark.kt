package render.sparks

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.XY

@Serializable
sealed class Spark {

    val xy = XY(0,0)
    var offsetX = 0f
    var offsetY = 0f
    var scale = 1f
    var alpha = 1f
    var progress = 0f
    var done = false

    fun at(x: Int, y: Int): Spark = this.apply { xy.x = x ; xy.y = y }

    open fun duration() = 1f
    open fun isLit() = true

    open fun glyph() = Glyph.BLANK
    open fun offsetX() = offsetX
    open fun offsetY() = offsetY
    open fun scale() = scale
    open fun alpha() = alpha

    open fun onRender(delta: Float) {
        progress += delta / duration()
        if (progress > 1f) {
            done = true
        }
    }

}
