package world

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import things.LightSource
import things.Temporal
import things.Thing
import things.ThingHolder
import util.XY
import util.hasOneWhere
import util.log
import world.stains.Stain

@Serializable
class CellContainer : ThingHolder {

    @Transient override var level: Level? = null
    val xy = XY(0,0)
    val contents = mutableListOf<Thing>()
    val stains = mutableListOf<Stain>()

    override fun xy() = xy
    override fun contents() = contents

    fun reconnect(level: Level, x: Int, y: Int) {
        xy.x = x
        xy.y = y
        this.level = level
        contents.forEach { thing ->
            thing.onRestore(this)
            if (thing is Temporal) level.linkTemporal(thing)
        }
        stains.forEach { stain ->
            stain.holder = this
            level.linkTemporal(stain)
        }
    }

    fun unload() {
        contents.forEach { it.holder = null }
        level = null
        contents.clear()
    }

    override fun add(thing: Thing) {
        contents.add(thing)
        if (level == null) log.info("adding thing to cell $xy but level is null!")
        level?.onAddThing(xy.x, xy.y, thing)
    }

    override fun remove(thing: Thing) {
        contents.remove(thing)
        level?.onRemoveThing(xy.x, xy.y, thing)
    }

    fun addStain(stain: Stain) {
        stains.firstOrNull { it.stackType() == stain.stackType() }?.also { oldStain ->
            oldStain.stackWith(stain)
        } ?: run {
            stain.holder = this
            stains.add(stain)
            level?.linkTemporal(stain)
        }
    }

    fun expireStain(stain: Stain) {
        stains.remove(stain)
    }

    fun topStain() = if (stains.isNotEmpty()) stains.last() else null

}
