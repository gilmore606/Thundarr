package actors.actions

import actors.Actor
import actors.stats.Speed
import world.level.Level
import java.lang.Float.max

abstract class Action(
    val duration: Float
) {

    // How many turns will this action take?
    open fun durationFor(actor: Actor) = this.duration * max(0.3f, (1f - (Speed.bonus(actor)) * 0.1f))

    open fun canQueueFor(actor: Actor) = true

    // Do whatever happens when actor does this.
    abstract fun execute(actor: Actor, level: Level)

    // Should we do this again immediately?
    open fun shouldContinueFor(actor: Actor): Boolean = false
}
