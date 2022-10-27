package actors.actions

import actors.Actor
import actors.Player
import actors.animations.Hop
import render.sparks.Speak
import ui.panels.Console
import world.Level

class Converse(
    private val target: Actor
) : Action(2.0f) {

    override fun execute(actor: Actor, level: Level) {
        actor.level?.addSpark(Speak().at(actor.xy.x, actor.xy.y))
        actor.animation = Hop()

        if (actor is Player) {
            Console.say(target.dname().capitalize() + " seems uninterested in you.")
        }
    }

}
