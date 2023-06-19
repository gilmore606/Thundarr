package actors.actions

import actors.Actor
import actors.Player
import actors.animations.Hop
import audio.Speaker
import render.sparks.Speak
import ui.panels.Console
import world.level.Level

class Converse(
    private val target: Actor
) : Action(2.0f) {
    override fun name() = "speak"

    override fun execute(actor: Actor, level: Level) {
        actor.level?.addSpark(Speak().at(actor.xy.x, actor.xy.y))
        actor.animation = Hop()

        if (actor is Player) {
            if (!target.onConverse(actor)) {
                Console.say(target.dnamec() + " seems uninterested in you.")
            } else {
                Speaker.world(target.talkSound(actor), source = target.xy)
            }
        } else {
            Speaker.world(actor.talkSound(target), source = actor.xy)
        }
    }

}
