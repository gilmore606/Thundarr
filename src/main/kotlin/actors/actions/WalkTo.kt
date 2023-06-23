package actors.actions

import actors.Actor
import kotlinx.serialization.Serializable
import util.XY
import util.log
import world.level.Level
import world.path.Pather

@Serializable
class WalkTo(
    val x: Int,
    val y: Int
) : Action(1f) {
    override fun name() = "move"

    private var done = false
    private val dest = XY(x,y)

    override fun shouldContinueFor(actor: Actor): Boolean = !done && (actor.xy.x != x || actor.xy.y != y)

    override fun execute(actor: Actor, level: Level) {
        Pather.nextStep(actor, dest)?.also {
            if (level.isWalkableFrom(actor, actor.xy, it)) {
                Move(XY(it.x, it.y)).execute(actor, level)
            } else {
                log.info("walkTo failed at unwalkable step $it")
                done = true
            }
        } ?: run { done = true }
    }

}
