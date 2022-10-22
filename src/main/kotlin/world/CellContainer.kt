package world

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import things.Thing
import things.ThingHolder

@Serializable
class CellContainer : ThingHolder {

    @Transient var level: Level? = null
    @Transient var x: Int = 0
    @Transient var y: Int = 0

    override val contents = mutableListOf<Thing>()

    override fun add(thing: Thing) {
        contents.add(thing)
        level?.onAddThing(x, y, thing)
    }

    override fun remove(thing: Thing) {
        contents.remove(thing)
        level?.onRemoveThing(x, y, thing)
    }

}
