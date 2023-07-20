package things

import actors.stats.Stat
import kotlinx.serialization.Serializable
import render.tilesets.Glyph


@Serializable
sealed class GenericNecklace : Clothing() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.NECK_CHARM_WORN)
    }
    override fun glyph() = Glyph.NECK_CHARM
    override fun glyphTransform() = glyphTransform
    override val slot = Slot.NECK
    override fun weight() = 0.05f
    override fun armor() = 0f
}

@Serializable
class SabretoothCharm : GenericNecklace() {
    override val tag = Tag.SABRETOOTH_CHARM
    override fun name() = "sabretooth charm"
    override fun description() = "A trophy from your first kill reminds you of your inner strength."
    override fun statEffects() = mapOf(Stat.Tag.STR to 1f)
}
