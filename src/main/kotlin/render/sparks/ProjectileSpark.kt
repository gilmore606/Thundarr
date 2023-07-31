package render.sparks

import render.tilesets.Glyph
import util.distanceBetween

class ProjectileSpark(
    val glyph: Glyph,
    val destX: Int,
    val destY: Int,
    val speed: Float,
    val onLand: (()->Unit)? = null
) : Spark() {

    var speedX = 0f
    var speedY = 0f
    var seconds = 1f

    init {
        scale = 1f
        alpha = 1f
        pausesAction = true
        offsetY -= 0.5f
    }

    override fun at(x: Int, y: Int): Spark {
        seconds = distanceBetween(x, y, destX, destY) / speed
        speedX = (destX - x).toFloat() / seconds
        speedY = (destY - y).toFloat() / seconds
        return super.at(x, y)
    }

    override fun isLit() = true
    override fun duration() = seconds
    override fun glyph() = glyph

    override fun onRender(delta: Float) {
        super.onRender(delta)
        val arc = if (progress < 0.66f) 1f else -2f
        offsetX += speedX * delta
        offsetY += speedY * delta - arc * delta * speed * 0.2f
    }

    override fun onDone() {
        onLand?.invoke()
    }
}
