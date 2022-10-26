package render.sparks

import render.tilesets.Glyph

class HealthUp : Spark() {

    init {
        alpha = 1.1f
        offsetY = -0.4f
    }

    private val driftSpeed = 0.6f
    private val fadeSpeed = 1.0f

    override fun glyph() = Glyph.HEALTH_ICON
    override fun duration() = 1.5f

    override fun onRender(delta: Float) {
        super.onRender(delta)
        offsetY -= delta * driftSpeed
        alpha -= fadeSpeed * delta
    }
}
