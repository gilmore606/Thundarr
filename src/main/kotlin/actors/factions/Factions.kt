package actors.factions

import kotlinx.serialization.Serializable
import util.log
import world.quests.Quest

@Serializable
class Factions {

    private val factions: MutableMap<String, Faction> = mutableMapOf()
    private val quests: MutableMap<String, Quest> = mutableMapOf()

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

    fun addQuest(newQuest: Quest): String {
        quests[newQuest.id] = newQuest
        return newQuest.id
    }

    fun byID(id: String): Faction? {
        if (factions.containsKey(id)) return factions[id]
        return null
    }

    fun questByID(id: String): Quest? {
        if (quests.containsKey(id)) return quests[id]
        return null
    }

}
