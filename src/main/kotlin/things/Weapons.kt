package things

import actors.actors.Actor
import actors.bodyparts.Bodypart
import actors.stats.Heart
import actors.stats.Stat
import actors.stats.skills.*
import actors.statuses.Status
import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.modals.ExamineModal.StatLine
import util.Dice
import util.LightColor
import world.Entity
import world.terrains.Terrain

@Serializable
enum class Damage(
    val displayName: String,
) {
    CRUSH("crush"),

    CUT("cut"),

    PIERCE("pierce"),

    BURN("burn"),

    SHOCK("shock") {
        override fun addDamage(target: Actor, bodypart: Bodypart, amount: Float): Float {
            if (target.hasStatus(Status.Tag.WET)) {
                return amount * 1.5f
            }
            return amount
        }
    },

    CORRODE("corrode"),

    ;
    open fun addDamage(target: Actor, bodypart: Bodypart, amount: Float): Float = amount
}


@Serializable
sealed class Weapon : Gear() {
    abstract fun damageType(): Damage
    abstract fun damage(): Float
    open fun getDamage(wielder: Actor, roll: Float): Float = damage()
    open fun speed(): Float = 1f
    open fun accuracy(): Float = 0f
    open fun critThreshold(): Float = 5f
    open fun critMultiplier(): Float = 0.5f

    open fun hitSound() = Speaker.SFX.HIT
    open fun bounceSound() = Speaker.SFX.HIT
    open fun missSound() = Speaker.SFX.MISS

    open fun rollDamage(wielder: Actor, roll: Float): Float {
        val base = getDamage(wielder, roll)
        var damage = Dice.float(base * 0.5f, base)
        if (roll >= critThreshold() * 2) {
            damage *= (1f + critMultiplier() * 2f)
        } else if (roll >= critThreshold()) {
            damage *= (1f + critMultiplier())
        }
        return damage
    }

    override fun examineStats(compareTo: Entity?) = mutableListOf<StatLine>().apply {
        add(StatLine(isSpacer = true))
        add(StatLine(
            "speed", speed(), lowerBetter = true,
            compare = compareTo?.let { (it as Weapon).speed() }
        ))
        add(StatLine(
            "accuracy", accuracy(), showPlus = true,
            compare = compareTo?.let { (it as Weapon).accuracy() }
        ))
        add(StatLine(isSpacer = true))
        add(StatLine(
            "type", valueString = damageType().displayName
        ))
        add(StatLine(
            "damage", damage(),
            compare = compareTo?.let { (it as Weapon).damage() }
        ))
        add(StatLine(isSpacer = true))
        add(StatLine(
            "crit thresh", critThreshold(), lowerBetter = true,
            compare = compareTo?.let { (it as Weapon).critThreshold() }
        ))
        add(StatLine(
            "crit bonus", critMultiplier(),
            compare = compareTo?.let { (it as Weapon).critMultiplier() }
        ))
    }
}

@Serializable
sealed class MeleeWeapon : Weapon() {
    override val slot = Slot.MELEE
    override fun value() = 4
    override fun equipSelfMsg() = "You ready your %d for action."
    override fun unequipSelfMsg() = "You return your %d to its sheath."
    override fun equipOtherMsg() = "%Dn takes out %id."
    override fun unequipOtherMsg() = "%Dn puts away %p %d."

    open fun canChopTrees() = false

    open fun hitSelfMsg() = "You hit %dd's %part with your %i!"
    open fun hitOtherMsg() = "%Dn hits %dd's %part with %p %i!"
    open fun missSelfMsg() = "You miss."
    open fun missOtherMsg() = "%Dn misses %dd."

    override fun thrownDamage(thrower: Actor, roll: Float) = damage() * 0.5f

    open fun canDig(terrainType: Terrain.Type): Boolean = false
    open fun skill(): Skill = Fight
}

@Serializable
sealed class UnarmedWeapon : MeleeWeapon() {
    override fun damage() = 0f
    override fun glyph() = Glyph.BLANK
    override fun getDamage(wielder: Actor, roll: Float): Float = wielder.unarmedDamage()
}
