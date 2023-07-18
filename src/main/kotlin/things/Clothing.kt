package things

import actors.actors.Actor
import actors.stats.Brains
import actors.stats.Stat
import actors.stats.skills.Build
import actors.stats.skills.Fight
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Damage.*
import ui.modals.ExamineModal.StatLine
import world.Entity
import java.lang.Float.max

@Serializable
sealed class Clothing : Gear() {

    enum class Material(
        val displayName: String,
        private val vsCRUSH: Float,
        private val vsCUT: Float,
        private val vsPIERCE: Float,
        private val vsBURN: Float,
        private val vsSHOCK: Float,
        private val vsCORRODE: Float
    ) {
        CLOTH("cloth", 0.1f, 0.1f, 0f, 0f, 0.2f, 0f),
        HIDE("hide", 1f, 1f, 1f, 1f, 1f, 1f),
        SCALES("scales", 0.8f, 1.2f, 1.2f, 1f, 1f, 1f),
        SHELL("shell", 0.5f, 1.5f, 1.5f, 1.5f, 2f, 1.5f),
        PLASTIC("plastic", 0.7f, 1f, 0.5f, 0.7f, 1.3f, 0.4f),
        METAL("metal", 1f, 1.4f, 1f, 1f, 0.2f, 1.5f),
        ;
        fun modify(type: Damage, amount: Float) = when (type) {
            CRUSH -> vsCRUSH * amount
            CUT -> vsCUT * amount
            PIERCE -> vsPIERCE * amount
            BURN -> vsBURN * amount
            SHOCK -> vsSHOCK * amount
            CORRODE -> vsCORRODE * amount
            else -> amount
        }
    }

    override fun equipSelfMsg() = "You put on your %d."
    override fun unequipSelfMsg() = "You take off your %d."
    override fun equipOtherMsg() = "%Dn puts on %id."
    override fun unequipOtherMsg() = "%Dn takes off %p %d."
    override fun value() = 1

    open fun material(): Material = Material.CLOTH
    open fun armor() = 0f
    open fun deflect() = 0f

    open fun coldProtection() = 0f
    open fun heatProtection() = 0f
    open fun weatherProtection() = 0f

    fun armorVs(type: Damage) = material().modify(type, armor())

    fun reduceDamage(target: Actor, type: Damage, rawDamage: Float): Float =
        max(0f, rawDamage - armorVs(type))

    override fun examineStats(compareTo: Entity?) = mutableListOf<StatLine>().apply {
        add(StatLine(isSpacer = true))
        add(StatLine(
            "deflect", deflect(), showPlus = true,
            compare = compareTo?.let { (it as Clothing).deflect() }
        ))
        add(StatLine(
            "made of", valueString = material().displayName
        ))
        add(StatLine(isSpacer = true))
        Damage.values().forEach { damage ->
            add(StatLine(
                "vs ${damage.displayName}", armorVs(damage),
                compare = compareTo?.let { (it as Clothing).armorVs(damage) }
            ))
        }
        add(StatLine(isSpacer = true))
        add(StatLine(
            "insulation", coldProtection(),
            compare = compareTo?.let { (it as Clothing).coldProtection() }
        ))
        add(StatLine(
            "cooling", heatProtection(),
            compare = compareTo?.let { (it as Clothing).heatProtection() }
        ))
        add(StatLine(
            "weather", weatherProtection(),
            compare = compareTo?.let { (it as Clothing).weatherProtection() }
        ))
    }
}

@Serializable
sealed class GenericHat : Clothing() {
    override val slot = Slot.HEAD
    override fun heatProtection() = 0.5f
    override fun coldProtection() = 0.4f
    override fun weatherProtection() = 0.4f
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
    override fun coldProtection() = 0.6f
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
    override fun material() = Material.HIDE
    override fun armor() = 2f
    override fun coldProtection() = 0.8f
    override fun weatherProtection() = 0.6f
}

@Serializable
sealed class GenericAmulet : Clothing() {
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
class SabretoothCharm : GenericAmulet() {
    override val tag = Tag.SABRETOOTH_CHARM
    override fun name() = "sabretooth charm"
    override fun description() = "A trophy from your first kill reminds you of your inner strength."
    override fun statEffects() = mapOf(Stat.Tag.STR to 1f)
}

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
    override fun description() = "A leather animal hide.  You could make something out of it."
    override fun hue() = 3.4f
    override fun weight() = 0.6f
    override fun material() = Material.HIDE
    override fun armor() = 1f
}

@Serializable
class ScalyHide: GenericCloak() {
    override val tag = Tag.SCALY_HIDE
    override fun name() = "scaly hide"
    override fun description() = "A thick animal hide covered in rigid scales.  You could make something out of it."
    override fun hue() = -2.2f
    override fun weight() = 1f
    override fun material() = Material.SCALES
    override fun armor() = 1f
}

@Serializable
class TarpCloak : GenericCloak() {
    override val tag = Tag.TARP
    override fun name() = "tarp"
    override fun description() = "A bluish plastic tarp, resistant to wind and rain."
    override fun weight() = 0.3f
    override fun coldProtection() = 0.1f
    override fun material() = Material.PLASTIC
    override fun armor() = 0f
}
