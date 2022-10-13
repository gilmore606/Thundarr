package actors.actions.processes

import actors.Actor
import actors.actions.Action
import actors.actions.Move
import util.XY
import util.log
import world.Level

class WalkTo(
    level: Level,
    val x: Int,
    val y: Int
) : Action(1f) {

    private val stepMap = level.makeStepMap().apply { update(x, y) }

    private var done = false


    override fun shouldContinueFor(actor: Actor): Boolean = !done && (actor.xy.x != x || actor.xy.y != y)

    override fun execute(actor: Actor, level: Level) {
        stepMap.stepFrom(actor.xy)?.also { dir ->
            Move(dir).execute(actor, level)
        } ?: run {
            log.debug("Pathing failed for $actor to $x,$y.")
            done = true
        }
    }

}
