package render.sparks

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import kotlin.random.Random

@Serializable
class Smoke : Spark() {

    init {
        scale = 0.7f
        alpha = 0.8f
        offsetY = -0.5f
    }
    private val driftSpeed = 1.3f
    private val shrinkSpeed = -0.8f
    private val fadeSpeed = 1.2f
    private val jitterSpeed = 1.5f

    override fun isLit() = true
    override fun duration() = 0.9f
    override fun glyph() = Glyph.SMOKE_PUFF

    override fun onRender(delta: Float) {
        super.onRender(delta)
        offsetY -= delta * driftSpeed
        offsetX += (Random.nextFloat() - 1f) * delta * jitterSpeed
        scale -= shrinkSpeed * delta
        alpha -= fadeSpeed * delta
    }

}
