package actors.actions

import actors.actors.Actor
import audio.Speaker
import kotlinx.serialization.Serializable
import world.level.Level

@Serializable
class Bark(
    val sfx: Speaker.SFX
    ) : Action(0.5f) {
    override fun name() = "bark"

    override fun execute(actor: Actor, level: Level) {
        Speaker.world(sfx, source = actor.xy)
    }
}
