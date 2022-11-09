package world

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import things.Temporal
import things.Thing
import things.ThingHolder
import util.XY
import util.log
import util.safeForEach
import world.level.Level
import world.stains.Stain

@Serializable
class CellContainer : ThingHolder {

    @Transient override var level: Level? = null
    val xy = XY(0,0)
    val contents = mutableListOf<Thing>()
    val stains = mutableListOf<Stain>()
    @Transient var locked = false

    override fun xy() = xy
    override fun contents() = contents

    fun reconnect(level: Level, x: Int, y: Int) {
        locked = true
        xy.x = x
        xy.y = y
        this.level = level
        contents.safeForEach { thing ->
            thing.onRestore(this)
            if (thing is Temporal) level.linkTemporal(thing)
        }
        stains.safeForEach { stain ->
            stain.onRestore(this)
        }
        locked = false
    }

    fun unload() {
        locked = true
        contents.forEach { it.holder = null }
        stains.forEach { it.holder = null }
        level = null
        contents.clear()
        locked = false
    }

    override fun add(thing: Thing) {
        locked = true
        contents.add(thing)
        if (level == null) log.warn("adding thing to cell $xy but level is null!")
        if (thing is Temporal) level?.linkTemporal(thing)
        level?.onAddThing(xy.x, xy.y, thing)
        locked = false
    }

    override fun remove(thing: Thing) {
        locked = true
        contents.remove(thing)
        if (thing is Temporal) level?.unlinkTemporal(thing)
        level?.onRemoveThing(xy.x, xy.y, thing)
        locked = false
    }

    fun addStain(stain: Stain) {
        locked = true
        stains.firstOrNull { it.stackType() == stain.stackType() }?.also { oldStain ->
            oldStain.stackWith(stain)
        } ?: run {
            stain.holder = this
            stains.add(stain)
            level?.linkTemporal(stain)
        }
        locked = false
    }

    fun cleanStains() {
        var found = false
        for (i in 0 until stains.size) {
            if (!found && stains[i].done) {
                locked = true
                found = true
                stains.removeAt(i)
                locked = false
            }
        }
    }

}
