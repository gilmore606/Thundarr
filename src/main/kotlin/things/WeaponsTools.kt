package things

import actors.actors.Actor
import actors.stats.Stat
import actors.stats.skills.*
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.gearmods.GearMod
import world.gen.spawnsets.LootSet
import world.terrains.Terrain
import world.gen.spawnsets.LootSet.Variant
import things.gearmods.GearMod.Tag.*

// Simple lower-level weapons


@Serializable
class Brick : MeleeWeapon() {
    override val tag = Tag.BRICK
    override fun baseName() = "brick"
    override fun description() = "A squared hunk of stone.  Could be used to kill, or build."
    override fun skill() = Clubs
    override fun speed() = 1.2f
    override fun glyph() = Glyph.BRICK
    override fun glyphTransform() = Rock.transform
    override fun weight() = 0.4f
    override fun damageType() = Damage.CRUSH
    override fun damage() = 3f
    override fun thrownDamage(thrower: Actor, roll: Float) = super.thrownDamage(thrower, roll) + 1.5f
}

@Serializable
class Rock : MeleeWeapon() {
    companion object {
        val transform = GlyphTransform(Glyph.ROCK_WORN, 0f, 0f, false)
    }
    override val tag = Tag.ROCK
    override fun baseName() = "rock"
    override fun description() = "A chunk of rock.  You could throw it at someone."
    override fun skill() = Clubs
    override fun speed() = 1f
    override fun glyph() = Glyph.ROCK
    override fun glyphTransform() = transform
    override fun weight() = 0.3f
    override fun damageType() = Damage.CRUSH
    override fun damage() = 2f
    override fun thrownDamage(thrower: Actor, roll: Float) = super.thrownDamage(thrower, roll) + 1.5f
}

@Serializable
class Rebar : MeleeWeapon() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.STICK, 0.0f, -0.1f, false)
    }
    override val tag = Tag.REBAR
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.STICK
    override fun hue() = -0.3f
    override fun baseName() = "rebar"
    override fun description() = "A rusted length of metal bar, pulled from ancient ruins."
    override fun skill() = Clubs
    override fun speed() = 1.2f
    override fun weight() = 1f
    override fun damageType() = Damage.CRUSH
    override fun damage() = 4f
}

@Serializable
class Hammer : MeleeWeapon() {
    companion object {
        val sellVariants = listOf(
            Variant(2f, null),
            Variant(1f, LIGHT),
            Variant(1f, HEAVY, 2),
            Variant(1f, FINE, 3),
        )
    }
    override val tag = Tag.HAMMER
    override fun baseName() = "hammer"
    override fun description() = "A simple ball-peen hammer."
    override fun skill() = Clubs
    override fun speed() = 1f
    override fun glyph() = Glyph.HAMMER
    override fun weight() = 0.6f
    override fun damageType() = Damage.CRUSH
    override fun damage() = 5f
}

@Serializable
class Knife : MeleeWeapon() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.KNIFE_WORN)
        val variants = listOf(
            Variant(1f, RUSTY, 0, 2),
            Variant(1f, BENT, 0, 2),
            Variant(2f, null, 2, 4),
            Variant(1f, LIGHT, 2, 4),
            Variant(1f, HEAVY, 2, 5),
            Variant(1f, FINE, 3, 5),
        )
        val sellVariants = listOf(
            Variant(2f, null),
            Variant(1f, LIGHT),
            Variant(1f, HEAVY, 2),
            Variant(1f, FINE, 3),
        )
    }
    override val tag = Tag.KNIFE
    override fun baseName() = "knife"
    override fun description() = "A single-edged survival knife."
    override fun glyphTransform() = glyphTransform
    override fun skill() = Blades
    override fun speed() = 0.8f
    override fun glyph() = Glyph.KNIFE
    override fun weight() = 0.2f
    override fun damageType() = Damage.CUT
    override fun damage() = 3f
}

@Serializable
class StoneAxe : MeleeWeapon() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.AXE)
    }
    override val tag = Tag.STONE_AXE
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.AXE
    override fun hue() = 0.2f
    override fun baseName() = "stone axe"
    override fun description() = "A chipped stone and branch fashioned into a crude axe."
    override fun canChopTrees() = true
    override fun skill() = Axes
    override fun speed() = 1.5f
    override fun damageType() = Damage.CUT
    override fun damage() = 3f
}

@Serializable
class Axe : MeleeWeapon() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.AXE)
        val variants = listOf(
            Variant(1f, RUSTY, 0, 2),
            Variant(1f, BENT, 0, 2),
            Variant(2f, null, 2, 4),
            Variant(1f, LIGHT, 2, 4),
            Variant(1f, HEAVY, 2, 5),
            Variant(1f, FINE, 3, 5),
        )
        val sellVariants = listOf(
            Variant(2f, null),
            Variant(1f, LIGHT),
            Variant(1f, HEAVY, 2),
            Variant(1f, FINE, 3),
        )
    }
    override val tag = Tag.AXE
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.AXE
    override fun baseName() = "axe"
    override fun description() = "A woodsman's axe.  Looks like it could chop more than wood.  I'm talking about flesh here."
    override fun canChopTrees() = true
    override fun skill() = Axes
    override fun speed() = 1.3f
    override fun damageType() = Damage.CUT
    override fun damage() = 5f
}

@Serializable
class Pickaxe : MeleeWeapon() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.AXE)
        val variants = listOf(
            Variant(1f, RUSTY, 0, 2),
            Variant(1f, BENT, 0, 2),
            Variant(2f, null, 2, 4),
            Variant(1f, LIGHT, 2, 4),
            Variant(1f, HEAVY, 2, 5),
            Variant(1f, FINE, 3, 5),
        )
        val sellVariants = listOf(
            Variant(2f, null),
            Variant(1f, LIGHT),
            Variant(1f, HEAVY, 2),
            Variant(1f, FINE, 3),
        )
    }
    override val tag = Tag.PICKAXE
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.AXE
    override fun hue() = 5.2f
    override fun baseName() = "pickaxe"
    override fun description() = "A miner's pickaxe.  Looks like it could pick more than flesh.  I'm talking about stone here."
    override fun skill() = Dig
    override fun speed() = 1.3f
    override fun canDig(terrainType: Terrain.Type) = Terrain.get(terrainType).dataType == Terrain.Type.GENERIC_WALL
    override fun statEffects() = mutableMapOf<Stat.Tag, Float>().apply {
        this[Dig.tag] = 1f
    }
    override fun accuracy() = -2f
    override fun damageType() = Damage.PIERCE
    override fun damage() = 4f
}

@Serializable
class Pitchfork : MeleeWeapon() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.TRIDENT)
        val sellVariants = listOf(
            Variant(2f, null),
            Variant(1f, LIGHT),
            Variant(1f, HEAVY, 2),
            Variant(1f, FINE, 3),
        )
    }
    override val tag = Tag.PITCHFORK
    override fun glyph() = Glyph.TRIDENT
    override fun glyphTransform() = glyphTransform
    override fun baseName() = "pitchfork"
    override fun description() = "A humble farming tool, but also a decent stabbing tool."
    override fun skill() = Spears
    override fun speed() = 1.3f
    override fun accuracy() = -1f
    override fun damageType() = Damage.PIERCE
    override fun damage() = 4f
}
