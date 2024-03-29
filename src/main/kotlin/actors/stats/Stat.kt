package actors.stats

import actors.actors.Actor
import actors.actors.Player
import actors.stats.skills.*
import audio.Speaker
import kotlinx.serialization.Serializable
import ui.panels.Console
import util.Dice

val allStats = listOf(Strength, Speed, Brains, Heart, Senses)

abstract class Stat(
    val tag: Tag,
    val name: String
) {

    private val ipPerImprove = 5f

    enum class Tag { STR, SPD, BRN, HRT, SEN,
                     DIG, FIGHT, DODGE, THROW, BUILD, SURVIVE, MEDIC, SNEAK,
                    AXES, BOWS, CLUBS, GUNS, SPEARS, SWORDS,
                    SCIENCE, CRAFT,
    }

    companion object {
        fun get(tag: Tag) = when (tag) {
            Tag.STR -> Strength
            Tag.SPD -> Speed
            Tag.BRN -> Brains
            Tag.HRT -> Heart
            Tag.SEN -> Senses
            Tag.DIG -> Dig
            Tag.FIGHT -> Fight
            Tag.DODGE -> Dodge
            Tag.THROW -> Throw
            Tag.BUILD -> Build
            Tag.SURVIVE -> Survive
            Tag.MEDIC -> Medic
            Tag.SNEAK -> Sneak
            Tag.AXES -> Axes
            Tag.BOWS -> Bows
            Tag.CLUBS -> Clubs
            Tag.GUNS -> Guns
            Tag.SPEARS -> Spears
            Tag.SWORDS -> Blades
            Tag.SCIENCE -> Science
            Tag.CRAFT -> Craft
        }
    }

    @Serializable
    class Value(
        var base: Float,  // Actor's (cached) base earned rating
        var final: Float? = null, // Actor's (cached) final rating, adjusted by status effects / environment / etc
        var ip: Float = 0f
    )

    private val affects = mutableSetOf<Stat>()
    fun addDependent(dependent: Stat) = affects.add(dependent)

    abstract fun description(): String
    abstract fun verb(): String
    open fun improveChance() = 0.2f
    open fun improveMsg() = "You gain new insights into " + verb() + "."
    open fun examineSpecialStat(): String? = null
    open fun examineSpecialStatValue(actor: Actor): String? = null

    // Set the base value for actor.  Probably only use this in initial NPC spawn.
    fun set(actor: Actor, base: Float) { actor.stats[tag] = Value(base) }
    fun set(actor: Actor, base: Int) { set(actor, base.toFloat()) }

    // Get the current value for actor.
    // If the final cached value is null, refresh the cache.
    // If there is no value at all, set the default and refresh cache.
    fun get(actor: Actor) = actor.stats[tag]?.let { it.final ?: updateCached(actor) } ?: updateBase(actor, getDefaultBase(actor))

    // Get the base value.  Does not add a blank record if skill not known.  Probably only use this in stat displays.
    fun getBase(actor: Actor) = actor.stats[tag]?.base ?: getDefaultBase(actor)

    // Get the improvement level.  Usage as above.
    fun getImprovement(actor: Actor) = actor.stats[tag]?.ip ?: 0f

    // Invalidate cache for actor because something relevant changed
    fun touch(actor: Actor) {
        actor.stats[tag]?.also { it.final = null } ?: run { actor.stats[tag] = Value(getDefaultBase(actor)) }
        affects.forEach { it.touch(actor) }
    }

    // Calculate the total for this actor's base, given status effects, environment, etc
    open fun total(actor: Actor, base: Float) = base + statBonuses(actor)

    fun statBonuses(actor: Actor): Float {
        var total = 0f
        actor.statuses.forEach { status ->
            status.statEffects()[tag]?.also { total += it }
        }
        actor.gear.values.forEach { gear ->
            gear?.also { gear ->
                gear.statEffects()[tag]?.also { total += it }
            }
        }
        return total
    }

    open fun getDefaultBase(actor: Actor) = 10f

    // Change actor's base value, refill the cache, and return the new final value
    private fun updateBase(actor: Actor, newBase: Float): Float {
        actor.stats[tag]?.also { it.base = newBase } ?: run {
            actor.stats[tag] = Value(newBase)
        }
        return updateCached(actor)
    }

    // Recalculate actor's final value, cache it, and return it
    private fun updateCached(actor: Actor): Float {
        actor.stats[tag]?.also { it.final = total(actor, it.base) }
        onUpdate(actor, actor.stats[tag]!!.final!!)
        return actor.stats[tag]!!.final!!
    }

    open fun onUpdate(actor: Actor, newTotal: Float) { }

    // Roll a skill check and return the +/- result, possibly improving the skill.
    fun resolve(actor: Actor, difficulty: Float, noImprove: Boolean = false): Float {
        val effective = get(actor) + difficulty
        if (!noImprove && effective in 8.0..14.0) {
            val improveChance = improveChance() * (1f + (15.0 - effective) * 0.1f)
            if (Dice.chance(improveChance.toFloat())) {
                improve(actor)
            }
        }
        val roll = Dice.skillCheck()
        return effective - roll
    }

    // Gain an improvement point, possibly gaining a base level.
    fun improve(actor: Actor, fullLevel: Boolean = false) {
        val value = actor.stats[tag] ?: Value(0f)
        if (value.base < 0f) value.base = 0f
        value.ip += if (fullLevel) 100f else ipPerImprove
        if (value.ip >= 100f) {
            value.ip = value.ip - 100f
            value.base += 1f
            if (actor is Player) {
                Console.say(improveMsg())
                Speaker.ui(Speaker.SFX.UIAWARD)
            }
        }
        actor.stats[tag] = value
        updateCached(actor)
    }

    fun bonus(actor: Actor): Float = (10f - get(actor)) * 0.5f

}
