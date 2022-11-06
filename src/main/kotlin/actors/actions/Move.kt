package actors.actions

import actors.Actor
import actors.Player
import audio.Speaker
import util.XY
import util.distanceBetween
import util.log
import world.Level
import world.terrains.Terrain

class Move(
    private val dir: XY
) : Action(1.0f) {

    override fun durationFor(actor: Actor) = super.durationFor(actor) * (actor.level?.moveSpeedFor(actor, dir) ?: 1f)

    override fun execute(actor: Actor, level: Level) {
        if (!(dir.x in -1 .. 1 && dir.y in -1 .. 1)) {
            log.warn("Ack!  Move.execute for impossible dir $dir for $actor")
        }
        if (level.isWalkableFrom(actor.xy, dir)) {
            actor.stepSpark(dir)?.also {
                actor.level?.addSpark(it.at(actor.xy.x, actor.xy.y))
            }
            actor.stepSound(dir)?.also {
                val dist = if (actor is Player) 0f else distanceBetween(actor.xy.x, actor.xy.y, App.player.xy.x, App.player.xy.y)
                Speaker.world(it, distance = dist)
            }
            actor.moveTo(level, actor.xy.x + dir.x, actor.xy.y + dir.y)
            actor.animation = actor.stepAnimation(dir)
            if (dir.x != 0) actor.mirrorGlyph = dir.x < 0
        }
    }

}
