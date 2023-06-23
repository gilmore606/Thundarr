package actors.actions

import actors.Actor
import actors.Player
import actors.animations.Hop
import audio.Speaker
import kotlinx.serialization.Serializable
import render.sparks.Speak
import ui.panels.Console
import world.level.Level

@Serializable
class Converse(
    private val targetID: String
) : Action(2.0f) {
    override fun name() = "speak"

    override fun execute(actor: Actor, level: Level) {
        actor.level?.addSpark(Speak().at(actor.xy.x, actor.xy.y))
        actor.animation = Hop()

        App.level.director.getActor(targetID)?.also { target ->
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

}
