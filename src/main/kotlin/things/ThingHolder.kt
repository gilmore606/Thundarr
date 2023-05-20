package things

import kotlinx.serialization.Transient
import util.XY
import world.level.Level

interface ThingHolder {

    @Transient var level: Level?

    fun contents(): MutableList<Thing>
    fun xy(): XY
    fun add(thing: Thing)
    fun remove(thing: Thing)

    fun temperature(): Float = level?.temperatureAt(xy()) ?: 65f
}
