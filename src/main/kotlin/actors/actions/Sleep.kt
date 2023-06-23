package actors.actions

import actors.Actor
import actors.Player
import kotlinx.serialization.Serializable
import ui.panels.Console
import util.Dice
import world.level.Level

@Serializable
class Sleep : Action(2f) {

    override fun name() = "sleep"

    override fun execute(actor: Actor, level: Level) {
        if (actor is Player && Dice.chance(0.1f)) {
            Console.say("You dream of large women.")
        }
        actor.onSleep()
    }
}
