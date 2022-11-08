package actors.actions

import actors.Actor
import actors.Player
import ui.panels.Console
import util.Dice
import world.level.Level

class Sleep : Action(3f) {

    override fun name() = "sleep"

    override fun execute(actor: Actor, level: Level) {
        if (actor is Player && Dice.chance(0.1f)) {
            Console.say("You dream of large women.")
        }
    }
}
