package things

import actors.Actor
import actors.stats.Heart
import actors.stats.Stat
import actors.stats.skills.*
import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.LightColor
import world.terrains.Terrain

@Serializable
sealed class MeleeWeapon : Gear() {
    override val slot = Slot.MELEE
    override fun value() = 4
    override fun equipSelfMsg() = "You ready your %d for action."
    override fun unequipSelfMsg() = "You return your %d to its sheath."
    override fun equipOtherMsg() = "%Dn takes out %id."
    override fun unequipOtherMsg() = "%Dn puts away %p %d."

    override fun spawnContainers() = super.spawnContainers().apply {
        add(Tag.TABLE)
    }

    open fun canChopTrees() = false

    open fun hitSelfMsg() = "You hit %dd with your %i!"
    open fun hitOtherMsg() = "%Dn hits %dd with %p %i!"
    open fun missSelfMsg() = "You miss."
    open fun missOtherMsg() = "%Dn misses %dd."

    open fun hitSound() = Speaker.SFX.HIT
    open fun bounceSound() = Speaker.SFX.HIT
    open fun missSound() = Speaker.SFX.MISS

    override fun thrownDamage(thrower: Actor, roll: Float) = damage() * 0.5f

    open fun canDig(terrainType: Terrain.Type): Boolean = false
    open fun speed(): Float = 1f
    open fun skill(): Stat = Fight
    open fun accuracy(): Float = 0f
    open fun damage(): Float = 2f

}

@Serializable
class Fist : MeleeWeapon() {
    override val tag = Tag.FIST
    override fun glyph() = Glyph.BLANK
    override fun name() = "fist"
    override fun description() = "Bare knuckles."
    override fun hitSelfMsg() = "You punch %dd!"
    override fun hitOtherMsg() = "%Dn punches %dd!"
    override fun damage() = 1f
}

@Serializable
class Teeth : MeleeWeapon() {
    override val tag = Tag.TEETH
    override fun glyph() = Glyph.BLANK
    override fun name() = "teeth"
    override fun description() = "Sharp teeth."
    override fun hitSelfMsg() = "You bite %dd!"
    override fun hitOtherMsg() = "%Dn bites %dd!"
    override fun damage() = 1.5f
}

@Serializable
class Stick : MeleeWeapon(), Fuel {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.STICK, 0.0f, -0.1f, false)
    }
    override val tag = Tag.STICK
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.STICK
    override fun name() = "stick"
    override fun description() = "A sturdy wood branch.  You could hit people with it, or make something out of it."
    override fun skill() = Clubs
    override var fuel = 40f
    override fun onBurn(delta: Float): Float { return super<Fuel>.onBurn(delta) }
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
    override fun name() = "rebar"
    override fun description() = "A rusted length of metal bar, pulled from ancient ruins."
    override fun skill() = Clubs
}

@Serializable
class Brick : MeleeWeapon() {
    override val tag = Tag.BRICK
    override fun name() = "brick"
    override fun description() = "A squared hunk of stone.  Could be used to kill, or build."
    override fun skill() = Clubs
    override fun glyph() = Glyph.BRICK
    override fun weight() = 0.4f
    override fun thrownDamage(thrower: Actor, roll: Float) = super.thrownDamage(thrower, roll) + 1.5f
}

@Serializable
class Rock : MeleeWeapon() {
    override val tag = Tag.ROCK
    override fun name() = "rock"
    override fun description() = "A chunk of rock.  You could throw it at someone."
    override fun skill() = Clubs
    override fun glyph() = Glyph.ROCK
    override fun weight() = 0.3f
    override fun thrownDamage(thrower: Actor, roll: Float) = super.thrownDamage(thrower, roll) + 1.5f
}

@Serializable
class Hammer : MeleeWeapon() {
    override val tag = Tag.HAMMER
    override fun name() = "hammer"
    override fun description() = "A simple ball-peen hammer."
    override fun skill() = Clubs
    override fun glyph() = Glyph.HAMMER
    override fun weight() = 0.6f
}

@Serializable
class Knife : MeleeWeapon() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.KNIFE_WORN)
    }
    override val tag = Tag.KNIFE
    override fun name() = "knife"
    override fun description() = "A single-edged survival knife."
    override fun skill() = Blades
    override fun glyph() = Glyph.KNIFE
    override fun weight() = 0.2f
    override fun glyphTransform() = glyphTransform
}

@Serializable
class Gladius : MeleeWeapon() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.SWORD_WORN)
    }
    override val tag = Tag.GLADIUS
    override fun name() = "gladius"
    override fun description() = "A double edged iron short sword."
    override fun skill() = Blades
    override fun glyph() = Glyph.SWORD
    override fun weight() = 0.5f
    override fun glyphTransform() = glyphTransform
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
    override fun name() = "stone axe"
    override fun description() = "A chipped stone and branch fashioned into a crude axe."
    override fun skill() = Axes
    override fun speed() = 1.4f
    override fun damage() = 3f
    override fun canChopTrees() = true
}

@Serializable
class Axe : MeleeWeapon() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.AXE)
    }
    override val tag = Tag.AXE
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.AXE
    override fun name() = "axe"
    override fun description() = "A woodsman's axe.  Looks like it could chop more than wood.  I'm talking about flesh here."
    override fun skill() = Axes
    override fun speed() = 1.4f
    override fun damage() = 5f
    override fun canChopTrees() = true
}

@Serializable
class Pickaxe : MeleeWeapon() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.AXE)
    }
    override val tag = Tag.PICKAXE
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.AXE
    override fun hue() = 5.2f
    override fun name() = "pickaxe"
    override fun description() = "A miner's pickaxe.  Looks like it could pick more than flesh.  I'm talking about stone here."
    override fun skill() = Dig
    override fun canDig(terrainType: Terrain.Type) = Terrain.get(terrainType).dataType == Terrain.Type.GENERIC_WALL
    override fun statEffects() = mutableMapOf<Stat.Tag, Float>().apply {
        this[Dig.tag] = 1f
    }
    override fun speed() = 1.3f
    override fun accuracy() = -2f
    override fun damage() = 4f
}

@Serializable
class Pitchfork : MeleeWeapon() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.TRIDENT)
    }
    override val tag = Tag.PITCHFORK
    override fun glyph() = Glyph.TRIDENT
    override fun glyphTransform() = glyphTransform
    override fun name() = "pitchfork"
    override fun description() = "A humble farming tool, but also a decent stabbing tool."
    override fun skill() = Spears
    override fun speed() = 1.3f
    override fun accuracy() = -1f
    override fun damage() = 3f
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
    override fun name() = "sunsword"
    override fun description() = "The legendary Sunsword holds the power of sunlight.  Weirdly effective against robots."
    override fun skill() = Blades
    override fun statEffects() = mutableMapOf<Stat.Tag, Float>().apply {
        this[Heart.tag] = 1f
    }
    override fun light() = if (equipped) lightColor else null
    override fun onEquip(actor: Actor) {
        actor.level?.addLightSource(actor.xy.x, actor.xy.y, actor)
    }

    override fun onUnequip(actor: Actor) {
        actor.level?.removeLightSource(actor)
    }
}
