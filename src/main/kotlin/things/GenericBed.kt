package things

import kotlinx.serialization.Serializable
import render.tilesets.Glyph

@Serializable
sealed class GenericBed() : Thing() {
    override fun isPortable() = false
    override fun isOpaque() = false
    override fun flammability() = 0.7f
    override fun sleepComfort() = 0.6f
}

@Serializable
class Bed : GenericBed() {
    override val tag = Tag.THING_BED
    override fun glyph() = Glyph.BED
    override fun name() = "bed"
    override fun description() = "A simple twin bed with a rough woolen blanket."
}

@Serializable
class Bedroll : GenericBed() {
    override val tag = Tag.THING_BEDROLL
    override fun glyph() = Glyph.BEDROLL
    override fun name() = "bedroll"
    override fun description() = "A light but sturdy cotton bedroll for traveling."
    override fun isPortable() = true
    override fun weight() = 1.5f
    override fun sleepComfort() = 0.4f
}
