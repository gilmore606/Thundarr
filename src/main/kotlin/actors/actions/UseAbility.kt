package actors.actions

import actors.actions.events.Event
import actors.actors.Actor
import kotlinx.serialization.Serializable
import util.log
import world.level.Level

@Serializable
class UseAbility(
    val abilityID: String,
    val targetID: String,
    val abilityDuration: Float,
): Action(abilityDuration), Event {

    override fun name() = "use ability"

    override fun execute(actor: Actor, level: Level) {
        actor.getAbility(abilityID)?.also { ability ->
            App.level.director.getActor(targetID)?.also { target ->
                ability.doExecute(actor, level, target)
            }
        } ?: log.warn("Actor $actor tried to use non-existent ability ID $abilityID !")
    }

}
