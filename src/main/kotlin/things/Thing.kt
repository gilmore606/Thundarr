package things

import actors.Actor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.Screen
import util.aOrAn
import world.Entity
import world.Level

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
        TORCH
    }

    class Use(
        val command: String,
        val duration: Float,
        val canDo: (Actor)->Boolean,
        val toDo: (Actor, Level)->Unit
    )

    open fun uses(): Set<Use> = setOf()

    override fun description() =  ""

    override fun level() = holder?.level
    override fun xy() = holder?.xy()
    override fun glyphBatch() = Screen.thingBatch
    override fun uiBatch() = Screen.uiThingBatch

    open fun listName() = name().aOrAn()

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
