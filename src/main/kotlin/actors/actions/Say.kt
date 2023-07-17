package actors.actions

import actors.actors.Actor
import actors.actors.NPC
import kotlinx.serialization.Serializable
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
