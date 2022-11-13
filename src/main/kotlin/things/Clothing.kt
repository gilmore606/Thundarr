package things

import actors.stats.Brains
import actors.stats.Stat
import actors.stats.skills.Build
import actors.stats.skills.Fight
import kotlinx.serialization.Serializable
import render.tilesets.Glyph

@Serializable
sealed class Clothing : Gear() {
    override fun equipSelfMsg() = "You put on your %d."
    override fun unequipSelfMsg() = "You take off your %d."
    override fun equipOtherMsg() = "%Dn puts on %id."
    override fun unequipOtherMsg() = "%Dn takes off %p %d."
    override fun weight() = 1f

    open fun armor() = 0f

}

@Serializable
class HardHat : Clothing() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.HARD_HAT, 0.0f, -0.6f)
    }
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.HARD_HAT
    override fun name() = "hard hat"
    override fun description() = "A dented yellow plastic helmet labelled 'BEN LLI  ONSTRU TION'."
    override val slot = Slot.HEAD
    override fun weight() = 0.5f
    override fun armor() = 1f
    override fun statEffects() = mutableMapOf<Stat.Tag, Float>().apply {
        this[Build.tag] = 1f
    }
}

@Serializable
class HornedHelmet : Clothing() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.HORNED_HAT, 0.0f, -0.6f)
    }
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.HORNED_HAT
    override fun name() = "horned helmet"
    override fun description() = "A fur-lined metal skullcap affixed with two large curved ox horns."
    override val slot = Slot.HEAD
    override fun weight() = 1.8f
    override fun armor() = 2f
    override fun statEffects() = mutableMapOf<Stat.Tag, Float>().apply {
        this[Fight.tag] = 1f
        this[Brains.tag] = -1f
    }
}

@Serializable
class RiotHelmet : Clothing() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.HELMET, 0.0f, -0.5f)
    }
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.HELMET
    override fun name() = "riot helmet"
    override fun description() = "A shiny blue-black helmet made of some high tech material."
    override val slot = Slot.HEAD
    override fun weight() = 0.7f
    override fun armor() = 4f
}

@Serializable
class MokBoots : Clothing() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.GRAY_BOOTS_WORN)
    }
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.GRAY_BOOTS
    override fun name() = "mok boots"
    override fun description() = "Heavy stiff boots of tough leather pressed in layers."
    override val slot = Slot.FEET
    override fun weight() = 2f
    override fun armor() = 2f
}

@Serializable
class TravelBoots : Clothing() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.BROWN_BOOTS_WORN)
    }
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.BROWN_BOOTS
    override fun name() = "travel boots"
    override fun description() = "Fur-lined boots, ideal for long journeys."
    override val slot = Slot.FEET
    override fun weight() = 1.5f
    override fun armor() = 1f
}

@Serializable
class FurTunic : Clothing() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.RED_TUNIC_WORN)
    }
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.RED_TUNIC
    override fun name() = "fur tunic"
    override fun description() = "Despite the chic lack of arms, the thick fur seems quite cozy."
    override val slot = Slot.TORSO
    override fun weight() = 1f
    override fun armor() = 1f
}

@Serializable
class FurJacket : Clothing() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.RED_JACKET_WORN)
    }
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.RED_JACKET
    override fun name() = "fur jacket"
    override fun description() = "The innovation of arms makes this a sort of super-tunic, the ultimate in barbarian comfort."
    override val slot = Slot.TORSO
    override fun weight() = 2.5f
    override fun armor() = 2f
}

@Serializable
class SabretoothCharm : Clothing() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.NECK_CHARM_WORN)
    }
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.NECK_CHARM
    override fun name() = "sabretooth charm"
    override fun description() = "A trophy from your first kill reminds you of your inner strength."
    override val slot = Slot.NECK
    override fun weight() = 0.1f
    override fun armor() = 0f
    override fun statEffects() = mapOf(Stat.Tag.STR to 1f)
}
