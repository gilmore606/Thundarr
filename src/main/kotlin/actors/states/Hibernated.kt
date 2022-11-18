package actors.states

import kotlinx.serialization.Serializable

@Serializable
class Hibernated : State() {
    override fun wantsToAct() = false


}
