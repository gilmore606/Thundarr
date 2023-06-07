package actors.actions

import actors.Actor
import world.level.Level

abstract class Action(
    val duration: Float
) {
    abstract fun name(): String

    // How many turns will this action take?
    open fun durationFor(actor: Actor) = this.duration * actor.actionSpeed()

    open fun canQueueFor(actor: Actor) = true

    // Should this instance of an action actually be a different action?   (ex: Move becomes Open for a door)
    open fun convertTo(actor: Actor, level: Level): Action? = null

    // Do whatever happens when actor does this.
    abstract fun execute(actor: Actor, level: Level)

    // Should we do this again immediately?
    open fun shouldContinueFor(actor: Actor): Boolean = false

    open fun onCancel(actor: Actor) { }
}
