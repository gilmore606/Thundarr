package actors.actions

import actors.actors.Actor
import kotlinx.serialization.Serializable
import world.level.Level

@Serializable
sealed class Action(
    val duration: Float
) {
    abstract fun name(): String

    override fun toString() = "${name()}($duration)"

    // How many turns will this action take?
    open fun durationFor(actor: Actor) = this.duration * actor.actionSpeed()

    open fun canQueueFor(actor: Actor) = true

    // Should this instance of an action actually be a different action?   (ex: Move becomes Open for a door)
    open fun convertTo(actor: Actor, level: Level): Action? = null

    // Do any prep we might need at queue time
    open fun onQueue(actor: Actor) { }

    // Do whatever happens when actor does this.
    abstract fun execute(actor: Actor, level: Level)

    // Should we do this again immediately?
    open fun shouldContinueFor(actor: Actor): Boolean = false

    open fun onCancel(actor: Actor) { }
}
