package actors.actions

import actors.Actor
import actors.Player
import ui.panels.Console
import ui.panels.TimeButtons
import world.level.Level

class Wait(
    duration: Float
) : Action(duration) {
    override fun name() = "wait"

    override fun execute(actor: Actor, level: Level) {
        if (actor is Player && TimeButtons.state == TimeButtons.State.PAUSE) {
            Console.say("You wait a moment.")
        }
    }
}
