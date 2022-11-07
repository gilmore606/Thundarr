package things

import actors.Actor
import audio.Speaker
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.Screen
import render.tilesets.Glyph
import ui.panels.Console
import world.Entity
import world.Level
import world.terrains.Terrain
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
        if (thrownDamage(App.player, 6f) > defaultThrownDamage()) {
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

    // Move speed penalty to walk past/through this thing on the ground
    open fun moveSpeedPast(actor: Actor): Float? = null

    open fun thrownDamage(thrower: Actor, roll: Float) = defaultThrownDamage()
    private fun defaultThrownDamage() = min(weight() / 0.1f, 4f)
    open fun thrownAccuracy() = -1f
    open fun onThrownOn(target: Actor) { moveTo(target.xy.x, target.xy.y) }
    open fun onThrownAt(level: Level, x: Int, y: Int) { moveTo(level, x, y) }
    open fun thrownHitSound() = Speaker.SFX.ROCKHIT

    open fun toolbarName(): String? = null
    open fun toolbarUseTag(): UseTag? = null
    open fun toolbarAction(instance: Thing) {
        toolbarUseTag()?.also { tag ->
            val use = uses()[tag]!!
            if (use.canDo(App.player)) {
                App.player.queue(actors.actions.Use(instance, use.duration, use.toDo))
            }
        }
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
    override fun thrownDamage(thrower: Actor, roll: Float) = super.thrownDamage(thrower, roll) + 1.5f
}

@Serializable
class Bomb : Portable(), Temporal {
    override fun name() = "bomb"
    override fun description() = "A box of explosives."
    override fun glyph() = Glyph.CHEST
    override fun weight() = 1.5f

    var active = false
    var fuse = 0f

    val radius = 4

    override fun uses() = mapOf(
        UseTag.SWITCH to Use("activate" + name(), 0.5f,
            canDo = { !active },
            toDo = { actor, level ->
                active = true
                Console.sayAct("You activate %dd.", "%Dn activates %dd.", actor, this)
                level.linkTemporal(this@Bomb)
                fuse = 15f
            }))

    override fun advanceTime(delta: Float) {
        if (active) {
            if (fuse > 0f) {
                fuse -= delta
            } else {
                explode()
            }
        }
    }

    private fun explode() {
        holder?.also { holder ->
            holder.xy()?.also { xy ->
                Speaker.world(Speaker.SFX.EXPLODE, source = xy)
                val x0 = xy.x - radius
                val y0 = xy.y - radius
                for (ix in x0 .. x0 + radius * 2) {
                    for (iy in y0 .. y0 + radius * 2) {
                        holder.level?.setTerrain(ix, iy, Terrain.Type.TERRAIN_STONEFLOOR)
                    }
                }
            }
        }
        active = false
        moveTo(null)
    }
}
