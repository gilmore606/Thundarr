package things

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import util.XY
import world.CellContainer
import world.level.Level

interface ThingHolder {

    enum class Type { ACTOR, CONTAINER, CELL }
    @Serializable
    data class Key(
        val type: Type,
        val actorKey: String? = null,
        val containerKey: Thing.Key? = null,
        val cellKey: XY? = null,
    ) {
        fun getHolder(level: Level): ThingHolder? = when (type) {
            Type.ACTOR -> level.director.getActor(actorKey!!)
            Type.CONTAINER -> containerKey!!.getThing(level) as Container
            Type.CELL -> level.cellContainerAt(cellKey!!.x, cellKey!!.y)
        }
    }
    fun getHolderKey(): Key

    @Transient var level: Level?

    fun contents(): MutableList<Thing>
    fun xy(): XY
    fun add(thing: Thing)
    fun remove(thing: Thing)

    fun temperature(): Float = level?.temperatureAt(xy())?.toFloat() ?: 65f
}
