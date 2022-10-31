package render.sparks

import render.tilesets.Glyph

class Pow : Spark() {

    init {
        scale = 1.0f
        alpha = 0.7f

    }

    private val shrinkSpeed = -0.9f
    private val fadeSpeed = 1.2f

    override fun isLit() = false
    override fun duration() = 1f
    override fun glyph() = Glyph.POW_ICON

    override fun onRender(delta: Float) {
        super.onRender(delta)
        scale -= shrinkSpeed * delta
        alpha -= fadeSpeed * delta
    }

}
