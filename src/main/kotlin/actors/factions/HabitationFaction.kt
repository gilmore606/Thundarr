package actors.factions

import kotlinx.serialization.Serializable
import world.gen.features.Habitation
import world.gen.features.Village

@Serializable
class HabitationFaction(
    val villageName: String,
    val flavor: Habitation.Flavor,
) : Faction(
    "$villageName citizens"
) {
    override fun memberLove() = true

}
