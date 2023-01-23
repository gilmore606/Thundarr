package render.sparks

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.XY
import kotlin.random.Random

@Serializable
class Splash(
    val dir: XY
) : Spark() {

    init {
        scale = 0.6f
        alpha = 1.2f
        offsetY = 0f
    }
    private val driftSpeed = -0.12f
    private val shrinkSpeed = -1.3f
    private val fadeSpeed = 2.2f

    override fun isLit() = true
    override fun duration() = 0.7f
    override fun glyph() = Glyph.SMOKE_PUFF

    override fun onRender(delta: Float) {
        super.onRender(delta)
        offsetY += delta * (driftSpeed + Random.nextFloat() * 0.5f)
        scale -= shrinkSpeed * delta
        alpha -= fadeSpeed * delta
    }
}
