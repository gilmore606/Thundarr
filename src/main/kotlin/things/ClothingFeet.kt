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
class Shoes : GenericShoes() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.GRAY_BOOTS_WORN)
    }
    override val tag = Tag.SHOES
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.GRAY_BOOTS
    override fun name() = "shoes"
    override fun description() = "SImple peasant shoes of stiff canvas."
    override fun weight() = 0.5f
    override fun material() = Material.CLOTH
    override fun armor() = 1f
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
    override fun weatherProtection() = 1f
}

@Serializable
class TravelBoots : GenericShoes() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.BROWN_BOOTS_WORN)
    }
    override val tag = Tag.TRAVEL_BOOTS
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.BROWN_BOOTS
    override fun name() = "travel boots"
    override fun description() = "Fur-lined boots, ideal for long journeys."
    override fun weight() = 1.5f
    override fun material() = Material.HIDE
    override fun armor() = 1f
    override fun coldProtection() = 0.6f
    override fun weatherProtection() = 1f
}
