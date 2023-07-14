package things

import actors.Actor
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.LightColor

@Serializable
class Forge : LitThing() {
    init {
        active = true
    }

    override val tag = Tag.FORGE
    override fun name() = "forge"
    override fun description() = "A brick furnace and forge for ironworking."
    override fun glyph() = Glyph.FORGE
    override fun isPortable() = false
    override fun isOpaque() = false
    override fun isBlocking(actor: Actor) = true
    override val lightColor = LightColor(0.4f, 0.1f, 0f)
}
