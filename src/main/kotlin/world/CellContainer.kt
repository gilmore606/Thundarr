package world

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import things.LightSource
import things.Thing
import things.ThingHolder
import util.XY
import util.log

@Serializable
class CellContainer : ThingHolder {

    @Transient override var level: Level? = null
    override var xy = XY(0,0)

    override val contents = mutableListOf<Thing>()

    fun reconnect(level: Level, x: Int, y: Int) {
        xy.x = x
        xy.y = y
        this.level = level
        contents.forEach { thing ->
            thing.holder = this
            if (thing is LightSource) {
                level.dirtyLights[thing] = xy
            }
        }
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

}
