package things

import actors.Actor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.Screen
import render.tilesets.Glyph
import util.aOrAn
import world.Entity
import world.Level
import java.lang.Float.min
import java.lang.RuntimeException

@Serializable
sealed class Thing : Entity {
    abstract fun isOpaque(): Boolean
    abstract fun isBlocking(): Boolean
    abstract fun isPortable(): Boolean

    @Transient var holder: ThingHolder? = null

    enum class UseTag {
        SWITCH, CONSUME, OPEN, EQUIP, UNEQUIP, DESTROY
    }

    class Use(
        val command: String,
        val duration: Float,
        val canDo: (Actor)->Boolean,
        val toDo: (Actor, Level)->Unit
    )

    open fun thingTag() = name()
    open fun uses(): Map<UseTag, Use> = mapOf()

    override fun description() =  ""
    open fun listTag() = if (thingTag() == App.player.thrownTag) "(throwing)" else ""
    fun listName() = name() + " " + listTag()

    override fun examineInfo(): String {
        if (thrownDamage() > defaultThrownDamage()) {
            return "It looks like it would do extra damage when thrown."
        }
        return super.examineInfo()
    }

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

    open fun thrownDamage() = defaultThrownDamage()
    private fun defaultThrownDamage() = min(weight() / 0.1f, 4f)

    open fun onThrownAt(thrower: Actor, level: Level, x: Int, y: Int) {
        level.actorAt(x, y)?.also {
            it.takeDamage(thrownDamage())
            it.receiveAttack(thrower)
        }
        moveTo(level, x, y)
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
class Log : Portable() {
    override fun name() = "log"
    override fun description() = "Big, heavy, wood.  Better than bad.  Good."
    override fun glyph() = Glyph.LOG
}

@Serializable
class Brick : Portable() {
    override fun name() = "brick"
    override fun description() = "A squared hunk of stone.  Could be used to kill, or build."
    override fun glyph() = Glyph.BRICK
    override fun weight() = 0.4f
    override fun thrownDamage() = super.thrownDamage() + 1f
}
