package things

import actors.Actor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.GameScreen
import render.tilesets.Glyph
import util.LightColor
import util.XY
import util.log
import world.Level

@Serializable
sealed class Thing {
    abstract fun glyph(): Glyph
    abstract fun isOpaque(): Boolean
    abstract fun isBlocking(): Boolean
    abstract fun isPortable(): Boolean
    abstract fun name(): String
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

    open fun onWalkedOnBy(actor: Actor) { }

    fun moveTo(to: ThingHolder?) {
        holder?.remove(this)
        this.holder = to
        to?.add(this)
    }

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

@Serializable
class Axe : Portable() {
    override fun glyph() = Glyph.AXE
    override fun name() = "axe"
    override val kind = Kind.AXE
}
