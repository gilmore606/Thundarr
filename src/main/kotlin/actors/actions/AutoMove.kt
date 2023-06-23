package actors.actions

import actors.Actor
import actors.Player
import actors.actions.Action
import actors.actions.Move
import audio.Speaker
import kotlinx.serialization.Serializable
import util.XY
import world.level.Level

@Serializable
class AutoMove(
    val dir: XY
) : Action(1f) {
    override fun name() = "explore"

    private var done = false

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
        if (level.isWalkableFrom(actor, actor.xy, dir)) {
            Move(XY(dir.x, dir.y)).execute(actor, level)
        } else {
            done = true
        }
    }
}
