package actors.factions

import kotlinx.serialization.Serializable

@Serializable
class VillageFaction(
    val villageName: String,
) : Faction(
    "$villageName citizens"
) {

    override fun hateMemberAttacker() = true

}
