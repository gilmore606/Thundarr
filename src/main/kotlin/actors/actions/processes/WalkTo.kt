package actors.actions.processes

import actors.Actor
import actors.actions.Action
import actors.actions.Move
import util.DijkstraMap
import util.XY
import util.log
import world.EnclosedLevel
import world.Level

class WalkTo(
    level: Level,
    val x: Int,
    val y: Int
) : Action(1f) {

    private var stepMap: DijkstraMap

    private var done = false

    // TODO: fix this properly for WorldLevel, remove this bullshit
    init {
        if (level is EnclosedLevel) {
            stepMap = DijkstraMap(level.width, level.height) { x, y ->
                level.isWalkableAt(x, y)
            }.apply { update(XY(x, y)) }
        } else {
            stepMap = DijkstraMap(10, 10) { x, y -> true}
        }
    }

    override fun shouldContinueFor(actor: Actor): Boolean = !done && (actor.xy.x != x || actor.xy.y != y)

    override fun execute(actor: Actor, level: Level) {
        if (level !is EnclosedLevel) {
            done = true
            return
        }
        stepMap.stepFrom(actor.xy)?.also { dir ->
            Move(dir).execute(actor, level)
        } ?: run {
            log.debug("Pathing failed for $actor to $x,$y.")
            done = true
        }
    }

}
