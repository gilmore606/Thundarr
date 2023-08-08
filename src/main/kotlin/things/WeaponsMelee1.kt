package things

import actors.actors.Actor
import actors.stats.Heart
import actors.stats.Stat
import actors.stats.skills.Blades
import actors.stats.skills.Clubs
import actors.stats.skills.Spears
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.gearmods.GearMod
import util.LightColor
import world.gen.spawnsets.LootSet
import world.gen.spawnsets.LootSet.Variant
import things.gearmods.GearMod.Tag.*

// Basic melee weapons

@Serializable
class Club : MeleeWeapon() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.STICK)
        val variants = listOf(
            Variant(1f, BENT, 0, 2),
            Variant(2f, null, 1, 4),
            Variant(1f, LIGHT, 1, 4),
            Variant(1f, HEAVY, 2, 5),
            Variant(1f, FINE, 3, 5),
        )
        val sellVariants = listOf(
            Variant(2f, null, 2, 4),
            Variant(1f, LIGHT, 2, 5),
            Variant(1f, HEAVY, 2, 6),
            Variant(0.6f, FINE, 2, 6),
        )
    }
    override val tag = Tag.CLUB
    override fun baseName() = "club"
    override fun description() = "A sturdy wood bludgeon with a taped handle, bound with iron."
    override fun glyphTransform() = glyphTransform
    override fun skill() = Clubs
    override fun speed() = 1.1f
    override fun glyph() = Glyph.STICK
    override fun weight() = 1f
    override fun damageType() = Damage.CRUSH
    override fun damage() = 4f
}

@Serializable
class Gladius : MeleeWeapon() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.SWORD_WORN)
        val variants = listOf(
            Variant(1f, RUSTY, 1, 3),
            Variant(1f, BENT, 1, 3),
            Variant(2f, null, 2, 5),
            Variant(1f, LIGHT, 2, 5),
            Variant(1f, HEAVY, 3, 6),
            Variant(1f, FINE, 4, 6),
            Variant(0.5f, MASTER, 5, 8)
        )
        val sellVariants = listOf(
            Variant(2f, null, 2, 4),
            Variant(1f, LIGHT, 2, 5),
            Variant(1f, HEAVY, 2, 6),
            Variant(0.6f, FINE, 2, 6),
        )
    }
    override val tag = Tag.GLADIUS
    override fun baseName() = "gladius"
    override fun description() = "A double edged iron short sword."
    override fun glyphTransform() = glyphTransform
    override fun skill() = Blades
    override fun speed() = 0.9f
    override fun glyph() = Glyph.SWORD
    override fun weight() = 0.5f
    override fun damageType() = Damage.CUT
    override fun damage() = 5f
}

@Serializable
class Longsword : MeleeWeapon() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.SWORD_WORN)
        val variants = listOf(
            Variant(1f, RUSTY, 2, 4),
            Variant(1f, BENT, 2, 4),
            Variant(2f, null, 3, 6),
            Variant(1f, LIGHT, 3, 7),
            Variant(1f, HEAVY, 4, 7),
            Variant(1f, FINE, 4, 8),
            Variant(0.5f, MASTER, 5, 9)
        )
        val sellVariants = listOf(
            Variant(2f, null, 2, 4),
            Variant(1f, LIGHT, 3, 5),
            Variant(1f, HEAVY, 3, 6),
            Variant(0.6f, FINE, 4, 6),
        )
    }
    override val tag = Tag.LONGSWORD
    override fun baseName() = "longsword"
    override fun description() = "A single edged iron longsword."
    override fun glyphTransform() = glyphTransform
    override fun skill() = Blades
    override fun speed() = 1f
    override fun glyph() = Glyph.SWORD
    override fun hue() = 0.3f
    override fun weight() = 1.5f
    override fun damageType() = Damage.CUT
    override fun damage() = 6f
}

@Serializable
class Warhammer : MeleeWeapon() {
    companion object {
        val variants = listOf(
            Variant(1f, RUSTY, 2, 4),
            Variant(1f, BENT, 2, 4),
            Variant(2f, null, 3, 6),
            Variant(1f, HEAVY, 4, 7),
            Variant(1f, FINE, 4, 8),
            Variant(0.5f, MASTER, 5, 9)
        )
        val sellVariants = listOf(
            Variant(2f, null, 2, 4),
            Variant(1f, LIGHT, 3, 5),
            Variant(1f, HEAVY, 3, 6),
            Variant(0.6f, FINE, 4, 6),
        )
    }
    override val tag = Tag.WARHAMMER
    override fun baseName() = "gladius"
    override fun description() = "A double edged iron short sword."
    override fun skill() = Clubs
    override fun speed() = 1.3f
    override fun glyph() = Glyph.HAMMER
    override fun weight() = 2f
    override fun damageType() = Damage.CRUSH
    override fun damage() = 8f
    override fun accuracy() = -1f
    override fun critBonus() = 0.6f
}

@Serializable
class BronzeSpear : MeleeWeapon() {
    companion object {
        val variants = listOf(
            Variant(1f, RUSTY, 2, 4),
            Variant(1f, BENT, 2, 4),
            Variant(2f, null, 3, 6),
            Variant(1f, LIGHT, 3, 7),
            Variant(1f, HEAVY, 4, 7),
            Variant(1f, FINE, 4, 8),
            Variant(0.5f, MASTER, 5, 9)
        )
        val sellVariants = listOf(
            Variant(2f, null, 2, 4),
            Variant(1f, LIGHT, 3, 5),
            Variant(1f, HEAVY, 3, 6),
            Variant(0.6f, FINE, 4, 6),
        )
    }
    override val tag = Tag.BRONZE_SPEAR
    override fun baseName() = "bronze spear"
    override fun description() = "A stout pole tipped with a broad bronze spearhead."
    override fun skill() = Spears
    override fun speed() = 1.1f
    override fun glyph() = Glyph.SPEAR
    override fun weight() = 1.2f
    override fun damageType() = Damage.PIERCE
    override fun damage() = 6f
    override fun critBonus() = 0.6f
}

@Serializable
class SteelSpear : MeleeWeapon() {
    companion object {
        val variants = listOf(
            Variant(1f, RUSTY, 2, 5),
            Variant(1f, BENT, 2, 5),
            Variant(2f, null, 4, 7),
            Variant(1f, LIGHT, 4, 7),
            Variant(1f, HEAVY, 4, 8),
            Variant(1f, FINE, 5, 9),
            Variant(0.5f, MASTER, 6, 10)
        )
        val sellVariants = listOf(
            Variant(2f, null, 2, 4),
            Variant(1f, LIGHT, 3, 5),
            Variant(1f, HEAVY, 3, 6),
            Variant(0.6f, FINE, 4, 6),
        )
    }
    override val tag = Tag.STEEL_SPEAR
    override fun baseName() = "steel spear"
    override fun description() = "A stout pole tipped with a tapered steel spearhead."
    override fun skill() = Spears
    override fun speed() = 1.1f
    override fun glyph() = Glyph.SPEAR
    override fun hue() = 3.4f
    override fun weight() = 1.2f
    override fun damageType() = Damage.PIERCE
    override fun damage() = 7f
    override fun critBonus() = 0.7f
}

@Serializable
class Sunsword : MeleeWeapon(), LightSource {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.SUNSWORD_LIT)
        val lightColor = LightColor(0.1f, 0.3f, 0.3f)
    }
    override val tag = Tag.SUNSWORD
    override fun glyph() = if (equipped) Glyph.SUNSWORD_LIT else Glyph.SUNSWORD
    override fun glyphTransform() = glyphTransform
    override fun baseName() = "sunsword"
    override fun description() = "The legendary Sunsword holds the power of sunlight.  Weirdly effective against robots."
    override fun skill() = Blades
    override fun speed() = 0.9f
    override fun statEffects() = mutableMapOf<Stat.Tag, Float>().apply {
        this[Heart.tag] = 1f
    }
    override fun light() = if (equipped) lightColor else null
    override fun damageType() = Damage.SHOCK
    override fun damage() = 12f

    override fun onEquip(actor: Actor) {
        actor.level?.addLightSource(actor.xy.x, actor.xy.y, actor)
    }

    override fun onUnequip(actor: Actor) {
        actor.level?.removeLightSource(actor)
    }
}
