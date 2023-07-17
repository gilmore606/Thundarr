package actors.actions

import actors.actors.Actor
import actors.actors.Player
import kotlinx.serialization.Serializable
import ui.panels.Console
import ui.panels.TimeButtons
import world.level.Level

@Serializable
class Wait(
    private val waitDuration: Float
) : Action(waitDuration) {
    override fun name() = "wait"

    override fun execute(actor: Actor, level: Level) {
        if (actor is Player && TimeButtons.state == TimeButtons.State.PAUSE) {
            Console.say("You wait a moment.")
        }
    }
}
