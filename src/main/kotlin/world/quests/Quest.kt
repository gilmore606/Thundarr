package world.quests

import actors.Citizen
import actors.Villager
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import kotlinx.serialization.Serializable
import render.Screen
import ui.modals.ConverseModal
import util.UUID
import world.Chunk
import world.ChunkMeta
import world.gen.cartos.Carto
import world.gen.features.Feature
import world.gen.features.Habitation
import ui.modals.ConverseModal.*
import ui.panels.Console

@Serializable
sealed class Quest(

) : ConverseModal.Source {

    enum class State { AVAILABLE, REFUSED, OPEN, WON, LOST }
    var state: State = State.AVAILABLE
    fun shouldFlagGiver() = (state == State.AVAILABLE || state == State.OPEN)

    val id = UUID()

    var giverID: String? = null

    // Do we need a specific giver assigned?
    open fun needsGiver() = true

    // Could citizen give out this quest?
    open fun couldBeGivenBy(citizen: Citizen) = citizen is Villager

    // Modify the giver on spawn
    open fun onGiverSpawn(giver: Citizen) {
        giver.questsGiven.add(this.id)
        giverID = giver.id
    }

    // Player can choose one or the other on accept
    open fun cashReward(): Int? = 100
    open fun heartReward(): Int? = 1

    // Do necessary setup during Metamap generation
    open fun metaSetup(feature: Feature, source: Habitation) { }

    // Dig whatever we need into our target
    open fun doDig(feature: Feature, carto: Carto, meta: ChunkMeta, chunk: Chunk) { }

    // Dig whatever we need into our source
    open fun doDigSource(habitation: Habitation, carto: Carto, meta: ChunkMeta, chunk: Chunk) { }

    // Comments by non-giver citizens at source
    open fun sourceChatterMsg(): String? = null

    // Comment by giver to offer this quest
    open fun mentionMsg(): String = "I've got a job for you.  Interested?"
    open fun offerMsg(): String = "I need you to do a certain thing.  Will you help?"
    open fun thanksMsg(): String = "Thanks again for your help."
    open fun acceptMsg(): String = "Thanks, and good luck.  Come back when you've done it."
    open fun refuseMsg(): String = "That's too bad.  I suppose I'll find someone eventually."
    open fun remindMsg(): String = "You take care of that thing yet?"

    open fun commentLines(): List<String> = listOf()

    override fun getConversationTopic(topic: String): Scene? = when (topic) {
            "hello" -> when (state)  {
                State.AVAILABLE -> Scene(topic, mentionMsg(), listOf(
                    Option("quest_${id}_offer", "Tell me more about the job.")
                ))
                State.OPEN -> Scene(topic, remindMsg(), listOf())
                State.WON -> Scene("", thanksMsg(), listOf())
                else -> null
            }
            "quest_${id}_offer" -> Scene(topic, offerMsg(), listOf(
                Option("quest_${id}_accept", "I'll see what I can do.") { this.accept() },
                Option("quest_${id}_refuse", "I just don't have time.") { this.refuse() },
            ))
            "quest_${id}_accept" -> Scene(topic, acceptMsg(), listOf())
            "quest_${id}_refuse" -> Scene(topic, refuseMsg(), listOf())
            else -> null
        }

    protected fun accept() {
        state = State.OPEN
        Console.say("You accepted a quest!")
    }

    protected fun refuse() {
        state = State.REFUSED
    }

    fun dig(feature: Feature) {
        doDig(feature, feature.carto, feature.meta, feature.chunk)
    }

    fun digSource(habitation: Habitation) {
        doDigSource(habitation, habitation.carto, habitation.meta, habitation.chunk)
    }

}
