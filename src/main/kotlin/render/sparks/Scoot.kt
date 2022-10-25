package render.sparks

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.XY
import kotlin.random.Random

@Serializable
class Scoot(
    val dir: XY
    ) : Spark() {

    init {
        scale = 0.6f
        alpha = 1.2f
    }
    private val driftSpeed = 0.7f
    private val shrinkSpeed = -0.7f
    private val fadeSpeed = 1.6f

    override fun isLit() = true
    override fun duration() = 0.7f
    override fun glyph() = Glyph.SMOKE_PUFF

    override fun onRender(delta: Float) {
        super.onRender(delta)
        offsetX += dir.x * delta * (driftSpeed + Random.nextFloat() * 0.5f)
        offsetY += dir.y * delta * (driftSpeed + Random.nextFloat() * 0.5f)
        scale -= shrinkSpeed * delta
        alpha -= fadeSpeed * delta
    }
}
