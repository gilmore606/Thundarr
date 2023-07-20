package things

import kotlinx.serialization.Serializable
import render.tilesets.Glyph


@Serializable
sealed class GenericShoes : Clothing() {
    override val slot = Slot.FEET
    override fun coldProtection() = 0.4f
    override fun weatherProtection() = 0.6f
}

@Serializable
class MokBoots : GenericShoes() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.GRAY_BOOTS_WORN)
    }
    override val tag = Tag.MOKBOOTS
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.GRAY_BOOTS
    override fun name() = "mok boots"
    override fun description() = "Heavy stiff boots of tough leather pressed in layers."
    override fun weight() = 2f
    override fun material() = Material.HIDE
    override fun armor() = 3f
}

@Serializable
class TravelBoots : GenericShoes() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.BROWN_BOOTS_WORN)
    }
    override val tag = Tag.TRAVELBOOTS
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.BROWN_BOOTS
    override fun name() = "travel boots"
    override fun description() = "Fur-lined boots, ideal for long journeys."
    override fun weight() = 1.5f
    override fun material() = Material.HIDE
    override fun armor() = 1f
}
