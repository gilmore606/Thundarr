package actors.actions.processes

import actors.Actor
import actors.actions.Action
import actors.actions.Move
import kotlinx.coroutines.delay
import util.XY
import util.log
import world.Level
import world.path.Pather

class WalkTo(
    level: Level,
    val x: Int,
    val y: Int
) : Action(1f) {


    private var done = false
    private val dest = XY(x,y)

    override fun shouldContinueFor(actor: Actor): Boolean = !done && (actor.xy.x != x || actor.xy.y != y)

    override fun execute(actor: Actor, level: Level) {
        Pather.nextStep(actor, dest)?.also {
            if (level.isWalkableFrom(actor.xy, it)) {
                Move(XY(it.x, it.y)).execute(actor, level)
            } else {
                log.info("walkTo failed at unwalkable step $it")
                done = true
            }
        } ?: run { done = true }
    }

}
