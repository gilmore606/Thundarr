package actors.actions

import actors.actors.Actor
import kotlinx.serialization.Serializable
import world.level.Level

@Serializable
class Sleep : Action(5f) {

    override fun name() = "sleep"

    override fun execute(actor: Actor, level: Level) {
        actor.onSleep(duration)
    }
}
