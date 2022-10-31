package render.sparks

import render.tilesets.Glyph

class Raindrop : Spark() {

    init {
        scale = 0.6f
        alpha = (App.player.level?.rainIntensity ?: 0f) * 0.4f + 0.8f
    }

    private val fadeSpeed = 2.0f

    override fun isLit() = true
    override fun duration() = 0.2f
    override fun glyph() = Glyph.RAINDROP

    override fun onRender(delta: Float) {
        super.onRender(delta)
        alpha -= (fadeSpeed - (App.player.level?.rainIntensity ?: 0f)) * delta
    }

}
