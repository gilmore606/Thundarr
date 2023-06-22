package actors.factions

import kotlinx.serialization.Serializable
import world.gen.features.Village

@Serializable
class VillageFaction(
    val villageName: String,
    val flavor: Village.Flavor,
) : Faction(
    "$villageName citizens"
) {

    override fun hateMemberAttacker() = true

}
