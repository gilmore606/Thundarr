package actors.actions

import actors.Actor
import world.Level

abstract class Action(
    val duration: Float
) {

    // How many turns will this action take?
    open fun duration() = this.duration

    // Do whatever happens when actor does this.
    abstract fun execute(actor: Actor, level: Level)
}
