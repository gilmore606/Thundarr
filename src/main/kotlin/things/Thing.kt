package things

import actors.Actor
import actors.actions.Action
import audio.Speaker
import com.badlogic.gdx.graphics.glutils.ETC1
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.Screen
import render.tilesets.Glyph
import ui.modals.DirectionModal
import ui.panels.Console
import util.*
import world.Entity
import world.level.Level
import world.stains.Fire
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
        E0, E1, E2, E3, E4, E5, E6, E7, E8, E9,
        USE, USE_ON, SWITCH, SWITCH_ON, SWITCH_OFF, CONSUME, OPEN, CLOSE, EQUIP, UNEQUIP, DESTROY, TRANSFORM,
    }

    class Use(
        val command: String,
        val duration: Float,
        val canDo: ((Actor, Int, Int, Boolean)->Boolean) = { _,_,_,_ -> false },
        val toDo: ((Actor, Level, Int, Int)->Unit) = { _,_,_,_ -> }
    ) {
        companion object {
            fun enumeratedTag(n: Int) = when (n) {
                0 -> UseTag.E0
                1 -> UseTag.E1
                2 -> UseTag.E2
                3 -> UseTag.E3
                4 -> UseTag.E4
                5 -> UseTag.E5
                6 -> UseTag.E6
                7 -> UseTag.E7
                8 -> UseTag.E8
                9 -> UseTag.E9
                else -> UseTag.E1
            }
        }
    }

    open fun thingTag() = name()
    open fun uses(): Map<UseTag, Use> = mapOf()

    protected fun isHeldBy(actor: Actor) = actor.contents.contains(this)
    protected fun isAtFeet(actor: Actor) = holder?.let { it.xy() == actor.xy() } ?: false
    protected fun isNextTo(actor: Actor) = holder?.let { it.xy()?.let { xy ->
        Math.abs(xy.x - actor.xy.x) < 2 && Math.abs(xy.y - actor.xy.y) < 2
    }} ?: false
    protected fun isNextTo(x: Int, y: Int) = holder?.let { it.xy()?.let { xy ->
        Math.abs(xy.x - x) < 2 && Math.abs(xy.y - y) < 2
    }} ?: false

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

    open fun drawExtraGlyphs(toDraw: (Glyph,Float,Float,Float)->Unit) { }

    open fun weight() = 0.1f
    open fun flammability() = 0f
    open fun onBurn(delta: Float): Float { // return the amount of fuel we provided on this turn
        if (Dice.chance(flammability())) {
            moveTo(null)
            return 0f
        } else {
            return 1f * delta
        }
    }

    open fun onWalkedOnBy(actor: Actor) { }
    open fun bumpAction(): Action? = null

    open fun onRestore(holder: ThingHolder) {
        this.holder = holder
    }

    fun moveTo(to: ThingHolder?) {
        val from = holder
        holder?.remove(this)
        if (this is Temporal) holder?.level?.unlinkTemporal(this)
        this.holder = to
        to?.add(this)
        if (this is Temporal) holder?.level?.linkTemporal(this)
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
            if (use.canDo(App.player, App.player.xy.x, App.player.xy.y, false)) {
                App.player.queue(actors.actions.Use(tag, instance, use.duration, use.toDo, App.player.xy.x, App.player.xy.y))
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
class Brick : Portable() {
    override fun name() = "brick"
    override fun description() = "A squared hunk of stone.  Could be used to kill, or build."
    override fun glyph() = Glyph.BRICK
    override fun weight() = 0.4f
    override fun thrownDamage(thrower: Actor, roll: Float) = super.thrownDamage(thrower, roll) + 1.5f
}

@Serializable
class Lighter : Portable() {
    override fun name() = "lighter"
    override fun description() = "A brass cigarette lighter.  Handy for starting fires."
    override fun glyph() = Glyph.LIGHTER
    override fun weight() = 0.02f
    override fun uses() = mapOf(
        UseTag.USE to Use("light fire nearby", 2.0f,
            canDo = { actor,x,y,targ ->
                var canDo = false
                if (actor.xy.x == x && actor.xy.y == y) {
                    DIRECTIONS.forEach { if (hasTargetAt(it.x + x, it.y + y)) canDo = true }
                } else canDo = hasTargetAt(x,y)
                canDo && isHeldBy(actor)
            },
            toDo = { actor, level, x, y ->
                if (actor.xy.x == x && actor.xy.y == y) askDirection(actor, level)
                else lightFireAt(actor, level, XY(x,y))
            })
    )
    override fun toolbarName() = "light fire nearby"
    override fun toolbarUseTag() = UseTag.USE

    private fun hasTargetAt(x: Int, y: Int): Boolean = holder?.level?.thingsAt(x, y)?.hasOneWhere { it.flammability() > 0f } ?: false

    private fun askDirection(actor: Actor, level: Level) {
        Screen.addModal(DirectionModal("Light a fire in which direction?")
        { xy ->
            if (xy == NO_DIRECTION) {
                Console.say("Are you crazy?  You'd be standing in a fire!")
            } else {
                lightFireAt(actor, level, XY(actor.xy.x + xy.x, actor.xy.y + xy.y))
            }
        })
    }
    private fun lightFireAt(actor: Actor, level: Level, xy: XY) {
        level.addStain(Fire(), xy.x, xy.y)
        Console.sayAct("You start a fire.", "%Dn lights a fire.", actor)
    }
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
        UseTag.SWITCH to Use("activate " + name(), 0.5f,
            canDo = { actor,x,y,targ -> !active && ((targ && isNextTo(actor)) || (targ && !isHeldBy(actor))) },
            toDo = { actor, level, x, y ->
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
