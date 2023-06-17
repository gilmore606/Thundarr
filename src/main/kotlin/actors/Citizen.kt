package actors

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import world.gen.features.Village

@Serializable
sealed class Citizen : NPC() {
    @Transient var village: Village? = null
}
