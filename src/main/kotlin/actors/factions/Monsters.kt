package actors.factions

import kotlinx.serialization.Serializable

@Serializable
class Monsters : Faction(
    "monsters"
) {

    override fun hatesFaction(otherFaction: Faction) = otherFaction.isHuman()

}
