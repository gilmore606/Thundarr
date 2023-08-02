package render.sparks

import render.tilesets.Glyph
import util.Dice
import util.XYf

class HitDebris : Spark() {

    private val vec = XYf(Dice.float(-1f, 1f), Dice.float(-2.5f, -1.5f))
    private val fadeSpeed = 1.2f
    private val fallSpeed = 6f

    init {
        scale = 1f
        alpha = 1f
    }

    override fun isLit() = true
    override fun duration() = 1f
    override fun glyph() = Glyph.DEBRIS_PARTICLE

    override fun onRender(delta: Float) {
        super.onRender(delta)
        offsetX += vec.x * delta
        offsetY += vec.y * delta
        alpha -= fadeSpeed * delta
        vec.y += fallSpeed * delta
    }

}
