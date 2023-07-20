package things

import kotlinx.serialization.Serializable
import render.tilesets.Glyph


@Serializable
sealed class GenericShirt : Clothing() {
    override val slot = Slot.TORSO
    override fun coldProtection() = 0.2f
}

@Serializable
class FurTunic : GenericShirt() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.RED_TUNIC_WORN)
    }
    override val tag = Tag.FURTUNIC
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.RED_TUNIC
    override fun name() = "fur tunic"
    override fun description() = "Despite the chic lack of arms, the thick fur seems quite cozy."
    override fun weight() = 1f
    override fun armor() = 1f
    override fun material() = Material.FUR
    override fun coldProtection() = 0.6f
    override fun weatherProtection() = 0.2f
}


@Serializable
class LeatherVest : GenericShirt() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.RED_TUNIC_WORN)
    }
    override val tag = Tag.LEATHERVEST
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.RED_TUNIC
    override fun hue() = 0.2f
    override fun name() = "leather vest"
    override fun description() = "A stiff vest cut from thick animal hide."
    override fun weight() = 1.3f
    override fun armor() = 1f
    override fun material() = Material.HIDE
    override fun coldProtection() = 0.2f
}

@Serializable
class ScaleVest : GenericShirt() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.RED_TUNIC_WORN)
    }
    override val tag = Tag.SCALEVEST
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.RED_TUNIC
    override fun hue() = 0.7f
    override fun name() = "scale vest"
    override fun description() = "A stiff vest cut from scaly reptile skin."
    override fun weight() = 1f
    override fun armor() = 1.5f
    override fun material() = Material.SCALES
    override fun coldProtection() = 0.2f
}

@Serializable
class LeatherJacket : GenericShirt() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.RED_JACKET_WORN)
    }
    override val tag = Tag.LEATHERJACKET
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.RED_JACKET
    override fun hue() = 0.2f
    override fun name() = "leather jacket"
    override fun description() = "A cool-looking jacket cut from thick animal hide."
    override fun weight() = 2.1f
    override fun material() = Material.HIDE
    override fun armor() = 2f
    override fun coldProtection() = 0.5f
    override fun weatherProtection() = 0.6f
}

@Serializable
class ScaleJacket : GenericShirt() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.RED_JACKET_WORN)
    }
    override val tag = Tag.SCALEJACKET
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.RED_JACKET
    override fun hue() = 0.7f
    override fun name() = "scale jacket"
    override fun description() = "A very cool-looking jacket cut from scaly reptile skin."
    override fun weight() = 1.6f
    override fun material() = Material.SCALES
    override fun armor() = 2.5f
    override fun coldProtection() = 0.6f
    override fun weatherProtection() = 0.8f
}

@Serializable
class FurJacket : GenericShirt() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.RED_JACKET_WORN)
    }
    override val tag = Tag.FURJACKET
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.RED_JACKET
    override fun name() = "fur jacket"
    override fun description() = "The innovation of arms makes this a sort of super-tunic, the ultimate in barbarian comfort."
    override fun weight() = 2.5f
    override fun material() = Material.FUR
    override fun armor() = 2f
    override fun coldProtection() = 0.8f
    override fun weatherProtection() = 0.7f
}
