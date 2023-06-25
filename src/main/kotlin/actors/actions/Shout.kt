package actors.actions

import actors.Actor
import actors.actions.events.Event
import audio.Speaker
import kotlinx.serialization.Serializable
import render.sparks.Speak
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
        }
        broadcast(level, actor, actor.xy())
    }
}

@Serializable
class ShoutHelp(
    val helpText: String?,
) : Shout(helpText) { }

@Serializable
class ShoutAccuse(
    val accuseText: String?,
    val criminal: Actor,
) : Shout(accuseText) { }

@Serializable
class ShoutPraise(
    val praiseText: String?,
    val saint: Actor,
) : Shout(praiseText) { }
