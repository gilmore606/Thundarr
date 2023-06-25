package actors.actions

import actors.Actor
import actors.NPC
import actors.actions.events.Event
import audio.Speaker
import kotlinx.serialization.Serializable
import render.sparks.Speak
import util.log
import world.level.Level

@Serializable
sealed class Shout(
    val text: String?
) : Action(0.25f), Event {
    override fun name() = "shout"

    override fun execute(actor: Actor, level: Level) {
        text?.also { text ->
            actor.level?.addSpark(Speak().at(actor.xy.x, actor.xy.y))
            Speaker.world(actor.talkSound(actor), source = actor.xy)
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
) : Shout(accuseText) { }
