package things

import actors.Actor
import actors.stats.Stat
import actors.stats.skills.Dig
import actors.stats.skills.Fight
import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import world.terrains.Terrain

@Serializable
sealed class MeleeWeapon : Gear() {
    override val slot = Slot.MELEE
    override fun equipSelfMsg() = "You ready your %d for action."
    override fun unequipSelfMsg() = "You return your %d to its sheath."
    override fun equipOtherMsg() = "%Dn takes out %id."
    override fun unequipOtherMsg() = "%Dn puts away %p %d."

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
    override fun glyph() = Glyph.BLANK
    override fun name() = "fist"
    override fun description() = "Bare knuckles."
    override fun hitSelfMsg() = "You punch %dd!"
    override fun hitOtherMsg() = "%Dn punches %dd!"
    override fun damage() = 1f
}

@Serializable
class Teeth : MeleeWeapon() {
    override fun glyph() = Glyph.BLANK
    override fun name() = "teeth"
    override fun description() = "Sharp teeth."
    override fun hitSelfMsg() = "You bite %dd!"
    override fun hitOtherMsg() = "%Dn bites %dd!"
    override fun damage() = 1.5f
}

@Serializable
class Axe : MeleeWeapon() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.AXE, 0.0f, -0.1f, false)
    }
    override fun glyphTransform() = glyphTransform
    override fun glyph() = Glyph.AXE
    override fun name() = "axe"
    override fun description() = "A woodsman's axe.  Looks like it could chop more than wood.  I'm talking about flesh here."
    override fun speed() = 1.4f
    override fun damage() = 5f
}

@Serializable
class Pickaxe : MeleeWeapon() {
    companion object {
        val glyphTransform = GlyphTransform(Glyph.AXE, 0.0f, -0.1f, false)
    }
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
