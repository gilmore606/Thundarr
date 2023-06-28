package world.quests

import kotlinx.serialization.Serializable
import world.gen.features.Feature
import world.gen.features.Habitation

@Serializable
sealed class Quest(

) {

    @Serializable
    enum class State { AVAILABLE, REFUSED, OPEN, WON, LOST }
    var state: State = State.AVAILABLE

    // Player can choose one or the other on accept
    open fun cashReward(): Int = 100
    open fun heartReward(): Int = 1

    // Do necessary setup during Metamap generation
    open fun metaSetup(feature: Feature, source: Habitation) { }

}
