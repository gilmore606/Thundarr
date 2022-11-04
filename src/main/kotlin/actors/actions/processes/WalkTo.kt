package actors.actions.processes

import actors.Actor
import actors.actions.Action
import actors.actions.Move
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

    private var lastStepTime = System.currentTimeMillis()

    override fun shouldContinueFor(actor: Actor): Boolean = !done && (actor.xy.x != x || actor.xy.y != y)

    override fun execute(actor: Actor, level: Level) {
        Pather.nextStep(actor, dest)?.also {
            log.info("nextstep $it")
            Move(XY(it.x - actor.xy.x, it.y - actor.xy.y)).execute(actor, level)
        } ?: run { done = true }
    }

}
