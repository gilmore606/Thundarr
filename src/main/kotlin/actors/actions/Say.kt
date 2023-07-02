package actors.actions

import actors.Actor
import actors.NPC
import audio.Speaker
import kotlinx.serialization.Serializable
import render.sparks.Speak
import ui.panels.Console
import world.level.Level

@Serializable
class Say(
    private val text: String = "",
    private val randomComment: Boolean = false
) : Action(0.5f) {
    override fun name() = "speak"
    override fun toString() = "Say($text)"
    override fun execute(actor: Actor, level: Level) {
        if (randomComment && actor is NPC) {
            actor.spoutComment()
        } else {
            actor.say(text)
        }
    }
}
