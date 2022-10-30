package actors.actions

import actors.Actor
import kotlinx.serialization.Serializable
import world.Level

@Serializable
abstract class Action(
    val duration: Float
) {

    // How many turns will this action take?
    open fun duration() = this.duration

    open fun canQueueFor(actor: Actor) = true

    // Do whatever happens when actor does this.
    abstract fun execute(actor: Actor, level: Level)

    // Should we do this again immediately?
    open fun shouldContinueFor(actor: Actor): Boolean = false
}
