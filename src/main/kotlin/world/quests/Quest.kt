package world.quests

import kotlinx.serialization.Serializable

@Serializable
sealed class Quest(

) {

    // Player can choose one or the other on accept
    open fun cashReward(): Int = 100
    open fun heartReward(): Int = 1

}
