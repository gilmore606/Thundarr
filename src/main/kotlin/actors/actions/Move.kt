package actors.actions

import actors.Actor
import util.XY
import util.log
import world.Level

class Move(
    private val dir: XY
) : Action(1.0f) {

    override fun execute(actor: Actor, level: Level) {
        if (!(dir.x in -1 .. 1 && dir.y in -1 .. 1)) {
            log.warn("Ack!  Move.execute for impossible dir $dir for $actor")
        }
        if (level.isWalkableFrom(actor.xy, dir)) {
            actor.stepSpark(dir)?.also {
                actor.level?.addSpark(it.at(actor.xy.x, actor.xy.y))
            }
            actor.moveTo(level, actor.xy.x + dir.x, actor.xy.y + dir.y)
            actor.animation = actor.stepAnimation(dir)
            if (dir.x != 0) actor.mirrorGlyph = dir.x < 0
        }
    }

}
