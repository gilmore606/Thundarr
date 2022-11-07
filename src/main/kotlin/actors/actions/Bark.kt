package actors.actions

import actors.Actor
import audio.Speaker
import world.level.Level

class Bark(
    val sfx: Speaker.SFX
    ) : Action(0.5f) {

    override fun execute(actor: Actor, level: Level) {
        Speaker.world(sfx, source = actor.xy)
    }
}
