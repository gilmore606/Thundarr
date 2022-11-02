package actors.stats

import actors.Actor
import actors.stats.skills.Dig
import actors.stats.skills.Fight
import actors.stats.skills.Throw
import kotlinx.serialization.Serializable

val allStats = listOf(Strength, Speed, Brains)

abstract class Stat(
    val tag: Tag,
    val name: String
) {

    enum class Tag { STR, SPD, BRN,
                     DIG, FIGHT, THROW }
    companion object {
        fun get(tag: Tag) = when (tag) {
            Tag.STR -> Strength
            Tag.SPD -> Speed
            Tag.BRN -> Brains
            Tag.DIG -> Dig
            Tag.FIGHT -> Fight
            Tag.THROW -> Throw
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

    // Set the base value for actor.  Probably only use this in initial NPC spawn.
    fun set(actor: Actor, base: Float) { actor.stats[tag] = Value(base) }

    // Get the current value for actor.
    // If the final cached value is null, refresh the cache.
    // If there is no value at all, set the default and refresh cache.
    fun get(actor: Actor) = actor.stats[tag]?.let { it.final ?: updateCached(actor) } ?: updateBase(actor, getDefaultBase(actor))

    // Get the base value.  Does not add a blank record if skill not known.  Probably only use this in stat displays.
    fun getBase(actor: Actor) = actor.stats[tag]?.base ?: getDefaultBase(actor)

    // Invalidate cache for actor because something relevant changed
    fun touch(actor: Actor) {
        actor.stats[tag]?.also { it.final = null } ?: run { actor.stats[tag] = Value(getDefaultBase(actor)) }
        affects.forEach { it.touch(actor) }
    }

    // Calculate the total for this actor's base, given status effects, environment, etc
    open fun total(actor: Actor, base: Float) = base

    open fun getDefaultBase(actor: Actor) = 10f

    abstract fun description(): String

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
        return actor.stats[tag]?.final ?: throw RuntimeException("update found no stat")
    }

}
