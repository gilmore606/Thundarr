package actors.actions

import actors.actors.Actor
import actors.actors.Player
import audio.Speaker
import kotlinx.serialization.Serializable
import util.XY
import world.level.Level

@Serializable
class AutoMove(
    val startDir: XY,
    val isHallway: Boolean = false
) : Action(1f) {
    override fun name() = "run"

    private var isFirstMove = true
    private var done = false
    private var dir = startDir

    override fun shouldContinueFor(actor: Actor): Boolean {
        if ((actor is Player) && !done) {
            if (!actor.isAutoActionSafe()) {
                Speaker.ui(Speaker.SFX.UIERROR)
                return false
            }
            return true
        }
        return false
    }

    override fun durationFor(actor: Actor) = super.durationFor(actor) * (actor.level?.moveSpeedFor(actor, dir) ?: 1f)

    override fun execute(actor: Actor, level: Level) {
        if (isHallway) {
            val free = actor.freeCardinalMoves().toMutableList()
            if (free.isEmpty()) {
                done = true
            } else if (!isFirstMove && (free.size > 2 || free.size == 1)) {
                done = true
            } else {
                if (dir !in free) {
                    free.remove(dir.flipped())
                    if (free.isEmpty()) done = true
                    else dir = free.first()
                }
                Move(XY(dir.x, dir.y)).execute(actor, level)
            }
        } else if (level.isWalkableFrom(actor, actor.xy, dir)) {
            Move(XY(dir.x, dir.y)).execute(actor, level)
        } else {
            done = true
        }
        isFirstMove = false
    }
}
