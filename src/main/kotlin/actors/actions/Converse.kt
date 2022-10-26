package actors.actions

import actors.Actor
import actors.Player
import ui.panels.Console
import world.Level

class Converse(
    private val target: Actor
) : Action(2.0f) {

    override fun execute(actor: Actor, level: Level) {
        if (actor is Player) {
            Console.say(target.dname().capitalize() + " seems uninterested in you.")
        }
    }

}
