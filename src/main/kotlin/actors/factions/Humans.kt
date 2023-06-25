package actors.factions

import kotlinx.serialization.Serializable

@Serializable
class Humans : Faction(
    "humans"
) {

    override fun hatesFaction(otherFaction: Faction) = otherFaction is Monsters

}
