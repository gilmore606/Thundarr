package render.sparks

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice

@Serializable
class Gore : Spark() {

    val dirx = Dice.float(-2f, 2f)
    val diry = Dice.float(-2.5f, -0.2f)
    val fadeSpeed = 1.2f
    var gravity = -0.4f
    val gravityForce = 5f
    init {
        alpha = 1.5f
        scale = 0.5f
    }

    override fun glyph() = Glyph.BLOODSTAIN
    override fun duration() = 1.2f
    override fun isLit() = true

    override fun onRender(delta: Float) {
        offsetX += dirx * delta
        offsetY += diry * delta
        offsetY += gravity * delta * gravityForce
        alpha -= fadeSpeed * delta
        gravity += delta * 2f
        if (offsetY > 0.4f) alpha = 0f
    }

}
