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

    // TODO: use the level's step map, this is stupid
    private val stepMap = level.makeStepMap().apply { update(x, y) }

    private var done = false

    private var lastStepTime = System.currentTimeMillis()

    override fun shouldContinueFor(actor: Actor): Boolean = !done && (actor.xy.x != x || actor.xy.y != y)

    override fun execute(actor: Actor, level: Level) {
        stepMap.stepFrom(actor.xy)?.also { dir ->
            Move(dir).execute(actor, level)
            val steptime = System.currentTimeMillis() - lastStepTime
            lastStepTime = System.currentTimeMillis()
            log.info("step time $steptime ms")
        } ?: run {
            log.debug("Pathing failed for $actor to $x,$y.")
            done = true
        }
    }

}
