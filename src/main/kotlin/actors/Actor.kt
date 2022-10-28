package actors

import actors.actions.Action
import actors.actions.Equip
import actors.actions.Unequip
import actors.animations.Animation
import actors.animations.Step
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.Screen
import render.sparks.HealthUp
import render.sparks.Scoot
import render.sparks.Spark
import render.tilesets.Glyph
import things.*
import util.*
import world.Entity
import world.Level
import world.stains.Blood
import java.lang.Float.max
import java.lang.Integer.min

@Serializable
sealed class Actor : Entity, ThingHolder, LightSource, Temporal {

    val id = shortID()
    var isUnloading = false
    val xy = XY(0,0)
    var juice = 0f // How many turns am I owed?
    val queuedActions: MutableList<Action> = mutableListOf()
    val contents = mutableListOf<Thing>()

    var hp = 20
    var hpMax = 20

    @Transient
    override var level: Level? = null
    @Transient
    val gear = mutableMapOf<Gear.Slot, Gear?>()

    @Transient
    var animation: Animation? = null
        set(value) {
            field = value
            value?.onStart()
        }

    open fun speed() = 1f
    open fun visualRange() = 22f

    open fun bleedChance() = 0.6f
    open fun stepAnimation(dir: XY) = Step(dir)
    open fun stepSpark(dir: XY): Spark? = Scoot(dir)

    override fun level() = level
    override fun xy() = xy
    override fun contents() = contents
    override fun glyphBatch() = Screen.actorBatch
    override fun uiBatch() = Screen.uiActorBatch

    abstract fun canAct(): Boolean
    abstract fun isActing(): Boolean

    fun onRestore() {
        contents.forEach { it.onRestore(this) }
        isUnloading = false
    }

    fun moveTo(level: Level?, x: Int, y: Int) {
        val oldX = this.xy.x
        val oldY = this.xy.y
        val oldLevel = this.level
        this.xy.x = x
        this.xy.y = y
        oldLevel?.onActorMovedFrom(this, oldX, oldY)
        if (oldLevel != level) oldLevel?.director?.detachActor(this)
        level?.onActorMovedTo(this, x, y)
        if (oldLevel != level) level?.director?.attachActor(this)
        onMove()
    }

    open fun onMove() { }

    open fun onConverse(actor: Actor): Boolean = false

    open fun statusGlyph(): Glyph? = null

    open fun receiveAttack(attacker: Actor) {
        if (Dice.chance(bleedChance())) {
            level?.addStain(Blood(), xy.x, xy.y)
            if (Dice.chance(0.4f)) {
                level?.addStain(Blood(), xy.x - 1 + Dice.zeroTo(2), xy.y - 1 + Dice.zeroTo(2))
            }
        }
    }

    // What will I do right now?
    fun nextAction(): Action? = if (queuedActions.isNotEmpty()) {
        val action = queuedActions[0]
        if (!action.shouldContinueFor(this)) {
            queuedActions.remove(action)
        }
        action
    } else defaultAction()

    // With nothing queued, what should I decide to do now?
    abstract fun defaultAction(): Action?

    // Queue an action to be executed next.
    fun queue(action: Action) {
        queuedActions.add(action)
    }

    override fun remove(thing: Thing) {
        contents.remove(thing)
        if (thing is LightSource && thing.light() != null) {
            level?.removeLightSource(this)
            level?.addLightSource(xy.x, xy.y, this)
        }
    }

    override fun add(thing: Thing) {
        contents.add(thing)
        if (thing is LightSource && light() != null) {
            level?.removeLightSource(this)
            level?.addLightSource(xy.x, xy.y, this)
        }
    }

    override fun light(): LightColor? {
        var light: LightColor? = null
        contents.forEach { thing ->
            if (thing is LightSource) {
                val thingLight = thing.light()
                if (thingLight != null) {
                    if (light == null) light = LightColor(0f,0f,0f)
                    light!!.r += thingLight.r
                    light!!.g += thingLight.g
                    light!!.b += thingLight.b
                }
            }
        }
        return light
    }

    fun animOffsetX() = animation?.offsetX() ?: 0f
    fun animOffsetY() = animation?.offsetY() ?: 0f

    final override fun onRender(delta: Float) {
        animation?.also { if (it.done) animation = null else it.onRender(delta) }
        doOnRender(delta)
    }
    open fun doOnRender(delta: Float) { }

    override fun advanceTime(delta: Float) { }

    fun equippedOn(slot: Gear.Slot): Gear? = gear[slot]

    fun equipGear(gear: Gear) {
        equippedOn(gear.slot)?.also { current ->
            queue(Unequip(current))
        }
        queue(Equip(gear))
    }

    fun unequipGear(gear: Gear) {
        if (gear.equipped) {
            queue(Unequip(gear))
        }
    }

    fun gainHealth(amount: Float) {
        hp = min(hpMax, (hp + amount).toInt())
        level?.addSpark(HealthUp().at(xy.x, xy.y))
    }

    fun takeDamage(amount: Float) {
        hp = max(0f, hp - amount).toInt()
        if (hp < 1) { die() }
    }

    open fun die() {
        val corpse = corpse()
        //contents.forEach { it.moveTo(corpse) }
        level?.also { corpse.moveTo(it, xy.x, xy.y) }
        onDeath()

        this.animation = null
        moveTo(null, 0, 0)
    }
    open fun corpse() = Corpse()
    open fun onDeath() { }
}
