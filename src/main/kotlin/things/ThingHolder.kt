package things

import kotlinx.serialization.Transient
import util.XY
import world.Level

interface ThingHolder {

    val contents: MutableList<Thing>
    val xy: XY
    @Transient var level: Level?

    fun add(thing: Thing)
    fun remove(thing: Thing)

    fun byKind() = mutableMapOf<Thing.Kind, MutableSet<Thing>>().apply {
        contents.forEach { thing ->
            if (containsKey(thing.kind)) {
                this[thing.kind]?.add(thing)
            } else {
                this[thing.kind] = mutableSetOf(thing)
            }
        }
    }
}
