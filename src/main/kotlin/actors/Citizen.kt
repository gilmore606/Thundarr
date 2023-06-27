package actors

import actors.actions.*
import actors.actions.events.Event
import actors.states.GoDo
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import util.Dice
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
        when (event) {
            is ShoutOpinion -> {
                when (event.opinion) {
                    Opinion.HATE -> downgradeOpinionOf(event.criminal)
                    Opinion.LOVE -> upgradeOpinionOf(event.criminal)
                    else -> { }
                }
            }
            is ShoutHelp -> {
                pushState(GoDo(location))
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
            is Smash -> {
                culprit?.also { culprit ->
                    say(listOf(
                        "What do you think you're doing?!", "You can't do that!", "Stop!"
                    ).random())
                    downgradeOpinionOf(culprit)
                }
            }
        }
        super.witnessEvent(culprit, event, location)
    }

    override fun pickAction(): Action? {
        if (Dice.chance(0.2f)) {
            val subjects = mutableSetOf<String>()
            entitiesSeen { it is Citizen }.keys.forEach { hearer ->
                subjects.addAll((hearer as Citizen).couldLearnFrom(this))
            }
            subjects.firstOrNull()?.also { subjectID ->
                App.level.director.getActor(subjectID)?.also { subject ->
                    val opinion = opinionOf(subject)
                    return ShoutOpinion(
                        if (opinion == Opinion.HATE) "Watch out for ${subject.dname()}!"
                        else "Did you hear about ${subject.dname()}?",
                        subject, opinion)
                }
            }
        }
        return null
    }
}
