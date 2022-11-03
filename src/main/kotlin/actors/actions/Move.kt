package actors.actions

import actors.Actor
import util.XY
import world.Level

class Move(
    private val dir: XY
) : Action(1.0f) {

    override fun execute(actor: Actor, level: Level) {
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
