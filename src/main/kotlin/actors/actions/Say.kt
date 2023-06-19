package actors.actions

import actors.Actor
import audio.Speaker
import render.sparks.Speak
import ui.panels.Console
import world.level.Level

class Say(
    private val text: String
) : Action(0.5f) {
    override fun name() = "speak"
    override fun execute(actor: Actor, level: Level) {
        actor.level?.addSpark(Speak().at(actor.xy.x, actor.xy.y))
        Speaker.world(actor.talkSound(actor), source = actor.xy)
        Console.sayAct("", "${actor.dnamec()} says, \"$text\"", actor)
    }
}
