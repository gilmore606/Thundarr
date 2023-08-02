package things

import actors.stats.Brains
import actors.stats.Stat
import actors.stats.skills.Build
import actors.stats.skills.Fight
import kotlinx.serialization.Serializable
import render.tilesets.Glyph


@Serializable
sealed class GenericHat : Clothing() {
    override val slot = Slot.HEAD
    override fun heatProtection() = 0.5f
    override fun coldProtection() = 0.4f
    override fun weatherProtection() = 0.4f
}

@Serializable
class WoolHat : GenericHat() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.HARD_HAT, 0.0f, -0.6f)
    }
    override val tag = Tag.WOOL_HAT
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.HARD_HAT
    override fun hue() = 3.5f
    override fun name() = "wool hat"
    override fun description() = "A close-fitting knitted woolen hat."
    override fun weight() = 0.1f
    override fun material() = Material.CLOTH
    override fun armor() = 1f
    override fun heatProtection() = 0f
    override fun coldProtection() = 1f
    override fun weatherProtection() = 0.6f
}

@Serializable
class HardHat : GenericHat() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.HARD_HAT, 0.0f, -0.6f)
    }
    override val tag = Tag.HARDHAT
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.HARD_HAT
    override fun name() = "hard hat"
    override fun description() = "A dented yellow plastic helmet labelled 'BEN LLI  ONSTRU TION'."
    override fun weight() = 0.5f
    override fun material() = Material.PLASTIC
    override fun armor() = 1f
    override fun statEffects() = mutableMapOf<Stat.Tag, Float>().apply {
        this[Build.tag] = 1f
    }
}

@Serializable
class HornedHelmet : GenericHat() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.HORNED_HAT, 0.0f, -0.6f)
    }
    override val tag = Tag.HORNEDHELMET
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.HORNED_HAT
    override fun name() = "horned helmet"
    override fun description() = "A fur-lined metal skullcap affixed with two large curved ox horns."
    override fun weight() = 1.8f
    override fun material() = Material.METAL
    override fun armor() = 2f
    override fun statEffects() = mutableMapOf<Stat.Tag, Float>().apply {
        this[Fight.tag] = 1f
        this[Brains.tag] = -1f
    }
}

@Serializable
class RiotHelmet : GenericHat() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.HELMET, 0.0f, -0.5f)
    }
    override val tag = Tag.RIOTHELMET
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.HELMET
    override fun name() = "riot helmet"
    override fun description() = "A shiny blue-black helmet made of some high tech material."
    override fun weight() = 0.7f
    override fun material() = Material.PLASTIC
    override fun armor() = 4f
}
