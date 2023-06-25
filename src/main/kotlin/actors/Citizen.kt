package actors

import actors.actions.*
import actors.actions.events.Event
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import util.XY
import util.log
import world.gen.features.Habitation

@Serializable
sealed class Citizen : NPC() {
    @Transient var habitation: Habitation? = null

    override fun receiveAggression(attacker: Actor) {
        if (!isHostileTo(attacker)) {
            super.receiveAggression(attacker)
            queue(ShoutHelp(
                "Help!"
            ))
        }
    }

    override fun witnessEvent(culprit: Actor?, event: Event, location: XY) {
        log.info("$this heard $event")
        when (event) {
            is ShoutOpinion -> {
                when (event.opinion) {
                    Opinion.HATE -> downgradeOpinionOf(event.criminal)
                    Opinion.LOVE -> upgradeOpinionOf(event.criminal)
                    else -> { }
                }
            }
            is Attack -> {
                culprit?.also { culprit ->
                    App.level.director.getActor(event.targetID)?.also { victim ->
                        if (opinionOf(victim) == Opinion.LOVE) {
                            downgradeOpinionOf(culprit)
                        }
                    }
                }
            }
        }
        super.witnessEvent(culprit, event, location)
    }
}
