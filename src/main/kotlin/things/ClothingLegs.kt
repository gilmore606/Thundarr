package things

import kotlinx.serialization.Serializable
import render.tilesets.Glyph

@Serializable
sealed class GenericPants : Clothing() {
    override val slot = Slot.LEGS
    override fun coldProtection() = 0.3f
}

@Serializable
class Jeans : GenericPants() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.RED_PANTS_WORN)
    }
    override val tag = Tag.JEANS
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.RED_PANTS
    override fun hue() = -3.1f
    override fun name() = "jeans"
    override fun description() = "Basic denim work jeans."
    override fun weight() = 1f
    override fun armor() = 2f
    override fun material() = Material.CLOTH
}

@Serializable
class LeatherPants : GenericPants() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.RED_PANTS_WORN)
    }
    override val tag = Tag.LEATHERPANTS
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.RED_PANTS
    override fun hue() = 0.2f
    override fun name() = "leather pants"
    override fun description() = "Heavy pants cut from animal hide."
    override fun weight() = 1.5f
    override fun armor() = 1f
    override fun material() = Material.HIDE
    override fun coldProtection() = 0.5f
}

@Serializable
class ScalePants : GenericPants() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.RED_PANTS_WORN)
    }
    override val tag = Tag.SCALEPANTS
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.RED_PANTS
    override fun hue() = 0.7f
    override fun name() = "scale pants"
    override fun description() = "Heavy pants cut from scaly reptile skin."
    override fun weight() = 1.5f
    override fun armor() = 1f
    override fun material() = Material.SCALES
    override fun coldProtection() = 0.5f
}

@Serializable
class FurPants : GenericPants() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.RED_PANTS_WORN)
    }
    override val tag = Tag.FURPANTS
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.RED_PANTS
    override fun name() = "fur pants"
    override fun description() = "Warm and stylish, but a bit smelly."
    override fun weight() = 1.5f
    override fun armor() = 1f
    override fun material() = Material.FUR
    override fun coldProtection() = 0.7f
    override fun weatherProtection() = 0.7f
}
