package render.sparks

import render.tilesets.Glyph
import util.distanceBetween

class ProjectileShadow(
    val destX: Int,
    val destY: Int,
    val speed: Float,
) : Spark() {

    var speedX = 0f
    var speedY = 0f
    var seconds = 1f

    init {
        scale = 1f
        alpha = 1f
    }

    override fun at(x: Int, y: Int): Spark {
        seconds = distanceBetween(x, y, destX, destY) / speed
        speedX = (destX - x).toFloat() / seconds
        speedY = (destY - y).toFloat() / seconds
        return super.at(x, y)
    }

    override fun isLit() = true
    override fun duration() = seconds
    override fun glyph() = Glyph.MOB_SHADOW

    override fun onRender(delta: Float) {
        super.onRender(delta)
        offsetX += speedX * delta
        offsetY += speedY * delta
    }
}
