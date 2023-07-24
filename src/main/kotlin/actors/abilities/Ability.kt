package actors.abilities

import actors.actors.Actor
import kotlinx.serialization.Serializable
import util.Dice
import util.UUID
import world.level.Level

@Serializable
sealed class Ability {

    val id = UUID()

    var lastUseTime: Double = 0.0
    open fun cooldown() = 0.0
    open fun useChance() = 1f

    open fun duration() = 1f
    open fun durationFor(actor: Actor) = duration() * actor.actionSpeed()

    open fun canQueue(actor: Actor, target: Actor): Boolean = ((App.gameTime.time - lastUseTime) > cooldown()) && Dice.chance(useChance())

    abstract fun shouldQueue(actor: Actor, target: Actor): Boolean

    fun doExecute(actor: Actor, level: Level, target: Actor) {
        lastUseTime = App.gameTime.time
        execute(actor, level, target)
    }

    abstract fun execute(actor: Actor, level: Level, target: Actor)

}
