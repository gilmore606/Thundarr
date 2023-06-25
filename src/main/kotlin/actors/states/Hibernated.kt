package actors.states

import kotlinx.serialization.Serializable

@Serializable
class Hibernated : State() {
    override fun toString() = "Hibernated"

    override fun wantsToAct() = false
}
