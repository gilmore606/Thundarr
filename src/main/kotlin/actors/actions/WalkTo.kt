package actors.actions

import actors.actors.Actor
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import util.XY
import util.log
import world.level.Level
import path.Pather
import util.manhattanDistance
import java.lang.Math.max
import java.lang.Math.min

@Serializable
class WalkTo(
    val x: Int,
    val y: Int
) : Action(1f) {
    override fun name() = "move"

    private var done = false
    private val dest = XY(x,y)

    override fun shouldContinueFor(actor: Actor): Boolean = !done && (actor.xy.x != x || actor.xy.y != y)

    override fun onQueue(actor: Actor) {
        Pather.subscribe(actor, dest, min(60, manhattanDistance(dest, actor.xy).toInt()))
    }

    override fun execute(actor: Actor, level: Level) {
        if (actor.xy == dest) done = true else {
            Pather.nextStep(actor, dest)?.also {
                if (level.isWalkableFrom(actor, actor.xy, it)) {
                    Move(XY(it.x, it.y)).execute(actor, level)
                } else {
                    log.info("walkTo failed at unwalkable step $it")
                    done = true
                }
            } ?: run { done = true }
        }
        if (done) {
            Pather.unsubscribe(actor, dest)
        }
    }

}
