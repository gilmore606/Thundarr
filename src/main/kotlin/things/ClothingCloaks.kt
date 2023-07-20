package things

import kotlinx.serialization.Serializable
import render.tilesets.Glyph


@Serializable
sealed class GenericCloak : Clothing() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.CLOAK_WORN)
    }
    override val slot = Slot.CLOAK
    override fun glyph() = Glyph.CLOAK
    override fun glyphTransform() = glyphTransform
    override fun weatherProtection() = 0.8f
    override fun heatProtection() = 0.4f
    override fun coldProtection() = 0.5f
}

@Serializable
class Hide : GenericCloak() {
    override val tag = Tag.ANIMAL_HIDE
    override fun name() = "hide"
    override fun description() = "A leather animal hide.  You could make something out of it, or wear it as a cloak."
    override fun hue() = 3.4f
    override fun weight() = 0.6f
    override fun material() = Material.HIDE
    override fun armor() = 1f
}

@Serializable
class FurHide : GenericCloak() {
    override val tag = Tag.FUR_HIDE
    override fun name() = "furry hide"
    override fun description() = "A leather animal hide covered in thick fur.  You could make something out of it, or wear it as a cloak."
    override fun hue() = 2.6f
    override fun weight() = 1.2f
    override fun material() = Material.FUR
    override fun armor() = 1f
}

@Serializable
class ScalyHide: GenericCloak() {
    override val tag = Tag.SCALY_HIDE
    override fun name() = "scaly hide"
    override fun description() = "A thick animal hide covered in rigid scales.  You could make something out of it, or wear it as a cloak."
    override fun hue() = -2.2f
    override fun weight() = 1f
    override fun material() = Material.SCALES
    override fun armor() = 1f
}

@Serializable
class TarpCloak : GenericCloak() {
    override val tag = Tag.TARP
    override fun name() = "tarp"
    override fun description() = "A bluish plastic tarp, resistant to wind and rain.  You could make something out of it, or wear it as a cloak."
    override fun weight() = 0.3f
    override fun coldProtection() = 0.1f
    override fun material() = Material.PLASTIC
    override fun armor() = 0f
}
