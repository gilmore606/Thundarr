package things

import actors.Actor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.Screen
import util.aOrAn
import world.Entity
import world.Level
import java.lang.RuntimeException

@Serializable
sealed class Thing : Entity {
    abstract fun isOpaque(): Boolean
    abstract fun isBlocking(): Boolean
    abstract fun isPortable(): Boolean
    abstract val kind: Kind

    @Transient var holder: ThingHolder? = null

    enum class Kind {
        LIGHTBULB,
        APPLE,
        AXE,
        SUNSWORD,
        ENERGY_DRINK,
        PINE_TREE,
        OAK_TREE,
        PALM_TREE,
        TORCH,
        CORPSE,
        MEAT,
        FILING_CABINET,
        HARD_HAT,
        HORNED_HELMET,
        RIOT_HELMET
    }

    class Use(
        val command: String,
        val duration: Float,
        val canDo: (Actor)->Boolean,
        val toDo: (Actor, Level)->Unit
    )

    open fun uses(): Set<Use> = setOf()

    override fun description() =  ""
    open fun listName() = name().aOrAn()

    override fun level() = holder?.level
    override fun xy() = holder?.xy()
    override fun glyphBatch() = Screen.thingBatch
    override fun uiBatch() = Screen.uiThingBatch

    open fun weight() = 0.1f

    open fun onWalkedOnBy(actor: Actor) { }

    open fun onRestore(holder: ThingHolder) {
        this.holder = holder
    }

    fun moveTo(to: ThingHolder?) {
        val from = holder
        holder?.remove(this)
        this.holder = to
        to?.add(this)
        onMoveTo(from, to)
    }
    fun moveTo(x: Int, y: Int) = moveTo(level()?.cellContainerAt(x, y) ?: throw RuntimeException("moved $this to local coords but it wasn't in a level!"))
    fun moveTo(level: Level, x: Int, y: Int) = moveTo(level.cellContainerAt(x, y))

    open fun onMoveTo(from: ThingHolder?, to: ThingHolder?) { }

}

@Serializable
sealed class Portable : Thing() {
    override fun isOpaque() = false
    override fun isBlocking() = false
    override fun isPortable() = true
}

@Serializable
sealed class Obstacle : Thing() {
    override fun isBlocking() = true
    override fun isPortable() = false
}
