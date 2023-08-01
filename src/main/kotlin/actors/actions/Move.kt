package actors.actions

import actors.actors.Actor
import audio.Speaker
import kotlinx.serialization.Serializable
import util.XY
import util.log
import world.level.Level

@Serializable
class Move(
    val dir: XY
) : Action(1.0f) {
    override fun name() = "move"

    override fun durationFor(actor: Actor) = super.durationFor(actor) * (actor.level?.moveSpeedFor(actor, dir) ?: 1f)

    override fun convertTo(actor: Actor, level: Level): Action? {
        return level.moveActionTo(actor, actor.xy.x, actor.xy.y, dir)
    }

    override fun execute(actor: Actor, level: Level) {
        if (!(dir.x in -1 .. 1 && dir.y in -1 .. 1)) {
            log.warn("Ack!  Move.execute for impossible dir $dir for $actor")
        }

        var moveOK = false
        if (level.isWalkableFrom(actor, actor.xy, dir)) {
            moveOK = true
        } else {
            level.actorAt(actor.xy.x + dir.x, actor.xy.y + dir.y)?.also { neighbor ->
                neighbor.moveTo(level, actor.xy.x, actor.xy.y)
                moveOK = true
            }
        }

        if (moveOK) {
            actor.stepSpark(dir)?.also {
                actor.level?.addSpark(it.at(actor.xy.x, actor.xy.y))
            }
            actor.stepSound(dir)?.also {
                Speaker.world(it, source = actor.xy)
            }
            actor.moveTo(level, actor.xy.x + dir.x, actor.xy.y + dir.y)
            actor.animation = actor.stepAnimation(dir)
            if (dir.x != 0) actor.mirrorGlyph = dir.x < 0
        }
    }

}
