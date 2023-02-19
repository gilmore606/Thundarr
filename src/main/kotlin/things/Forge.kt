package things

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.LightColor

@Serializable
class Forge : LitThing() {
    init {
        active = true
    }

    override fun name() = "forge"
    override fun description() = "A brick furnace and forge for ironworking."
    override fun glyph() = Glyph.FORGE
    override fun isPortable() = false
    override fun isOpaque() = false
    override fun isBlocking() = true
    override val lightColor = LightColor(0.5f, 0.15f, 0f)
}
