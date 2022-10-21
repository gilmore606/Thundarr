package world

import kotlinx.serialization.Serializable
import things.Thing
import things.ThingHolder

@Serializable
class CellContainer : ThingHolder {

    override val contents = mutableListOf<Thing>()
    override fun add(thing: Thing) { contents.add(thing) }
    override fun remove(thing: Thing) { contents.remove(thing) }

}
