package render.sparks

import kotlinx.serialization.Serializable
import render.tilesets.Glyph

@Serializable
class Raindrop : Spark() {

    init {
        scale = 0.6f
        alpha = (App.player.level?.weather?.rainIntensity ?: 0f) * 0.6f + 0.8f
    }

    private val fadeSpeed = 2.0f

    override fun isLit() = true
    override fun duration() = 0.2f
    override fun glyph() = Glyph.RAINDROP

    override fun onRender(delta: Float) {
        super.onRender(delta)
        alpha -= (fadeSpeed - (App.player.level?.weather?.rainIntensity ?: 0f)) * delta
    }

}
