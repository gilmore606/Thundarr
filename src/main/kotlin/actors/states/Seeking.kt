package actors.states

import kotlinx.serialization.Serializable

@Serializable
class Seeking(
    val targetID: String
) : State() {



}
