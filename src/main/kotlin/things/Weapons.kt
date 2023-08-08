package things

import actors.actors.Actor
import actors.bodyparts.Bodypart
import actors.stats.skills.*
import actors.statuses.Bleeding
import actors.statuses.Status
import actors.statuses.Stunned
import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.gearmods.WeaponMod
import ui.modals.ExamineModal.StatLine
import util.Dice
import util.total
import world.Entity
import world.terrains.Terrain

@Serializable
enum class Damage(
    val displayName: String,
    val bleedChance: Float,
) {
    CRUSH("crush", 0.1f) {
        override fun onDamage(target: Actor, bodypart: Bodypart?, amount: Float) {
            if (bodypart?.stunOnCrush == true) {
                val chance = (amount / target.hpMax()) * 1.5f
                if (Dice.chance(chance)) target.addStatus(Stunned())
            }
        }
    },

    CUT("cut", 0.5f) {
        override fun onDamage(target: Actor, bodypart: Bodypart?, amount: Float) {
            checkBleed(target, bodypart, amount)
        }
    },

    PIERCE("pierce", 0.3f) {
        override fun onDamage(target: Actor, bodypart: Bodypart?, amount: Float) {
            checkBleed(target, bodypart, amount)
        }
    },

    BURN("burn", 0f),

    SHOCK("shock", 0f) {
        override fun addDamage(target: Actor, bodypart: Bodypart, amount: Float): Float {
            if (target.hasStatus(Status.Tag.WET)) {
                return amount * 1.5f
            }
            return amount
        }
        override fun onDamage(target: Actor, bodypart: Bodypart?, amount: Float) {
            val shockChance = (amount / target.hpMax())
            if (Dice.chance(shockChance)) target.addStatus(Stunned())
        }
    },

    CORRODE("corrode", 0f),

    ;

    fun checkBleed(target: Actor, bodypart: Bodypart?, amount: Float) {
        val chance = (amount / target.hpMax())
        if (Dice.chance(chance)) {
            val perTick = (target.hpMax() / 20f) * Dice.float(0.5f, 1.5f)
            target.addStatus(Bleeding(perTick))
        }
    }

    open fun addDamage(target: Actor, bodypart: Bodypart, amount: Float): Float = amount
    open fun onDamage(target: Actor, bodypart: Bodypart?, amount: Float) { }
}


@Serializable
sealed class Weapon : Gear() {
    val mods = mutableListOf<WeaponMod>()
    fun mod(newMod: WeaponMod) { mods.add(newMod) }
    override fun canListGrouped() = mods.isEmpty()

    abstract fun baseName(): String
    override fun name() = mods.joinToString { it.prefix() } + baseName()

    abstract fun damageType(): Damage
    abstract fun damage(): Float
    open fun getDamage(wielder: Actor, roll: Float): Float = damage() + mods.total { it.damage() }

    open fun speed(): Float = 1f

    open fun accuracy(): Float = 0f
    open fun getAccuracy() = accuracy() + mods.total { it.accuracy() }

    override fun getWeight(): Float = weight() + mods.total { it.weight() }

    override fun getValue(): Int = (value() * mods.total { it.valueMod() }).toInt()

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
            "accuracy", getAccuracy(), showPlus = true,
            compare = compareTo?.let { (it as Weapon).getAccuracy() }
        ))
        add(StatLine(isSpacer = true))
        add(StatLine(
            "type", valueString = damageType().displayName
        ))
        add(StatLine(
            "damage", getDamage(App.player, 1f),
            compare = compareTo?.let { (it as Weapon).getDamage(App.player, 1f) }
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

    override fun thrownDamage(thrower: Actor, roll: Float) = getDamage(thrower, roll) * 0.5f

    open fun canDig(terrainType: Terrain.Type): Boolean = false
    open fun skill(): Skill = Fight
}

@Serializable
sealed class UnarmedWeapon : MeleeWeapon() {
    override fun damage() = 0f
    override fun glyph() = Glyph.BLANK
    override fun getDamage(wielder: Actor, roll: Float): Float = wielder.unarmedDamage()
}
