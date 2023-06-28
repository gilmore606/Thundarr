package world.quests

import actors.Citizen
import actors.Villager
import kotlinx.serialization.Serializable
import util.UUID
import world.Chunk
import world.ChunkMeta
import world.gen.cartos.Carto
import world.gen.features.Feature
import world.gen.features.Habitation

@Serializable
sealed class Quest(

) {

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
    open fun cashReward(): Int = 100
    open fun heartReward(): Int = 1

    // Do necessary setup during Metamap generation
    open fun metaSetup(feature: Feature, source: Habitation) { }

    // Dig whatever we need into our target
    open fun doDig(feature: Feature, carto: Carto, meta: ChunkMeta, chunk: Chunk) { }

    // Dig whatever we need into our source
    open fun doDigSource(habitation: Habitation, carto: Carto, meta: ChunkMeta, chunk: Chunk) { }

    // Comments by non-giver citizens at source
    open fun sourceChatterMsg(): String? = null

    // Comment by giver to offer this quest
    open fun sourceOfferMsg(): String = "I've got a job for you."

    fun dig(feature: Feature) {
        doDig(feature, feature.carto, feature.meta, feature.chunk)
    }

    fun digSource(habitation: Habitation) {
        doDigSource(habitation, habitation.carto, habitation.meta, habitation.chunk)
    }

}
