package actors.abilities

import actors.actors.Actor
import kotlinx.serialization.Serializable
import util.Dice
import util.UUID
import world.level.Level

@Serializable
sealed class Ability(
    val cooldown: Double = 0.0,
    val chance: Float = 1f,
) {

    val id = UUID()

    var lastUseTime: Double = 0.0

    open fun duration() = 1f
    open fun durationFor(actor: Actor) = duration() * actor.actionSpeed()

    open fun canQueue(actor: Actor, target: Actor): Boolean = ((App.gameTime.time - lastUseTime) > cooldown) && Dice.chance(chance)

    abstract fun shouldQueue(actor: Actor, target: Actor): Boolean

    fun doExecute(actor: Actor, level: Level, target: Actor) {
        lastUseTime = App.gameTime.time
        execute(actor, level, target)
    }

    abstract fun execute(actor: Actor, level: Level, target: Actor)

    open fun advanceTime(actor: Actor, delta: Float) { }
    open fun onDie(actor: Actor) { }
}
