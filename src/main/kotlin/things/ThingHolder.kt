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

}
