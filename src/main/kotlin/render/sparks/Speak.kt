package render.sparks

import render.tilesets.Glyph
import kotlin.random.Random

class Speak : Spark() {

    init {
        scale = 1.0f
        alpha = 0.8f
        offsetY = -0.5f
    }
    private val driftSpeed = 0.8f
    private val fadeSpeed = 1.2f

    override fun isLit() = false
    override fun duration() = 1.0f
    override fun glyph() = Glyph.SPEECH_BUBBLE

    override fun onRender(delta: Float) {
        super.onRender(delta)
        offsetY -= delta * driftSpeed
        alpha -= fadeSpeed * delta
    }

}
