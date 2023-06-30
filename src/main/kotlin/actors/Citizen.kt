package actors

import actors.actions.*
import actors.actions.events.Event
import actors.states.GoDo
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.tilesets.Glyph
import ui.modals.ConverseModal
import util.Dice
import util.XY
import world.gen.features.Habitation

@Serializable
sealed class Citizen : NPC(), ConverseModal.Source {
    @Transient var habitation: Habitation? = null

    val questsGiven = mutableListOf<String>()
    var hasQuestsToGive = false
    fun questsGiven() = questsGiven.mapNotNull { App.factions.questByID(it) }
    var introducedToPlayer = false

    override fun receiveAggression(attacker: Actor) {
        if (!isHostileTo(attacker)) {
            super.receiveAggression(attacker)
            queue(ShoutHelp("Help!"))
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

    override fun advanceTime(delta: Float) {
        questsGiven().forEach { quest ->
            if (quest.shouldFlagGiver()) hasQuestsToGive = true
        }
        super.advanceTime(delta)
    }

    override fun hasConversation() = hasQuestsToGive
    override fun conversationSources() = mutableListOf<ConverseModal.Source>(this).apply { addAll(questsGiven()) }
    override fun getConversationTopic(topic: String): ConverseModal.Scene? {
        if (topic == "hello") {
            return ConverseModal.Scene("",
                if (introducedToPlayer) "Hello again, Thundarr." else {
                    introducedToPlayer = true
                    "Nice to meet you Thundarr, I'm ${name()}."
                }, listOf())
        }
        return null
    }

    override fun commentLines(): List<String> {
        if (hasQuestsToGive) {
            val lines = mutableListOf<String>()
            questsGiven().forEach { quest ->
                lines.addAll(quest.commentLines())
            }
            if (lines.isNotEmpty()) return lines
        }
        return super.commentLines()
    }
}
