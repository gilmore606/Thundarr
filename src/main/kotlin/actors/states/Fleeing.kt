package actors.states

import kotlinx.serialization.Serializable

@Serializable
class Fleeing(
    val targetId: String
    ) : State() {



}
