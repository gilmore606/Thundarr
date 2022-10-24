package actors

import actors.actions.Action
import actors.actions.Equip
import actors.actions.Unequip
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import things.*
import util.*
import world.Entity
import world.Level

@Serializable
sealed class Actor : Entity, ThingHolder, LightSource, Temporal {

    override val xy = XY(0,0)  // position in current level
    var juice = 0f // How many turns am I owed?
    val queuedActions: MutableList<Action> = mutableListOf()

    @Transient
    override var level: Level? = null

    override val contents = mutableListOf<Thing>()
    @Transient
    val gear = mutableMapOf<Gear.Slot, Gear?>()

    open fun speed() = 1f
    open fun visualRange() = 22f

    override fun level() = level
    override fun xy() = xy

    fun onRestore() {
        contents.forEach { it.onRestore(this) }
    }

    open fun moveTo(level: Level, x: Int, y: Int, fromLevel: Level?) {
        this.xy.x = x
        this.xy.y = y
        fromLevel?.onActorMovedFrom(this, x, y, level)
        level.onActorMovedTo(this, x, y)
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
}
