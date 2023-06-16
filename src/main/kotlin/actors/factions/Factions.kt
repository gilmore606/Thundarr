package actors.factions

import kotlinx.serialization.Serializable

@Serializable
class Factions {

    private val factions: MutableMap<String, Faction> = mutableMapOf()

    var humans: String = ""
    var monsters: String = ""

    fun makeInitialFactions() {
        humans = addFaction(Humans())
        monsters = addFaction(Monsters())
    }

    fun addFaction(newFaction: Faction): String {
        factions[newFaction.id] = newFaction
        return newFaction.id
    }

    fun byID(id: String): Faction? {
        if (factions.containsKey(id)) return factions[id]
        return null
    }

}
