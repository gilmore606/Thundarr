package world.quests

import kotlinx.serialization.Serializable
import things.Paperback
import things.Thing
import world.gen.features.Feature
import world.gen.features.Habitation

@Serializable
class FetchQuest : Quest() {

    // When we spawn this item into the world, we clear this variable
    var unspawnedItem: Thing? = null
    var spawnedItemID: String? = null

    override fun toString() = "fetch $unspawnedItem"

    override fun metaSetup(feature: Feature, source: Habitation) {
        unspawnedItem = Paperback()
    }

    override fun commentLines() = listOf("Damn, why'd I ever lose it?", "I wish someone could help me.")

}
