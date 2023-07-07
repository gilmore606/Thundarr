package things

import kotlinx.serialization.Serializable
import render.tilesets.Glyph

@Serializable
sealed class Bed() : Thing() {
    override fun isPortable() = false
    override fun isOpaque() = false
    override fun flammability() = 0.4f
    override fun sleepComfort() = 0.6f
}

@Serializable
class Bedroll : Bed() {
    override val tag = Tag.THING_BEDROLL
    override fun glyph() = Glyph.BEDROLL
    override fun name() = "bedroll"
    override fun description() = "A light but sturdy cotton bedroll for traveling."
    override fun isPortable() = true
    override fun weight() = 1.5f
    override fun flammability() = 0.7f
    override fun sleepComfort() = 0.4f
}
