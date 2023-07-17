package actors.actions

import actors.actors.Actor
import actors.actors.NPC
import actors.actions.events.Event
import kotlinx.serialization.Serializable
import util.log
import world.level.Level

@Serializable
sealed class Shout(
    val text: String?
) : Action(0.25f), Event {
    override fun name() = "shout"
    override fun toString() = "Shout($text)"

    override fun eventSenses() = setOf(Event.Sense.AUDIBLE)

    override fun execute(actor: Actor, level: Level) {
        text?.also { text ->
            actor.say(text)
            log.info("SHOUTING: $this shouts $text")
        }
        broadcast(level, actor, actor.xy())
    }
}

@Serializable
class ShoutHelp(
    val helpText: String?,
) : Shout(helpText) { }

@Serializable
class ShoutOpinion(
    val accuseText: String?,
    val criminal: Actor,
    val opinion: NPC.Opinion = NPC.Opinion.HATE
) : Shout(accuseText) {
    override fun toString() = "ShoutAccuse($criminal, $opinion)"
}
