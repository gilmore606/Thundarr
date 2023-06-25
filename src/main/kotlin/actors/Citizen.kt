package actors

import actors.actions.*
import actors.actions.events.Event
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import util.XY
import world.gen.features.Village

@Serializable
sealed class Citizen : NPC() {
    @Transient var village: Village? = null

    override fun receiveAggression(attacker: Actor) {
        if (!isHostileTo(attacker)) {
            super.receiveAggression(attacker)
            queue(ShoutHelp(
                "Guards!  ${attacker.inamec()} is attacking me!"
            ))
            queue(ShoutAccuse(null, attacker))
        }
    }

    override fun witnessEvent(culprit: Actor?, event: Event, location: XY) {
        when (event) {
            is ShoutAccuse -> downgradeOpinionOf(event.criminal)
            is ShoutPraise -> upgradeOpinionOf(event.saint)
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
