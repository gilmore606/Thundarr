package actors

import actors.actions.Action
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.tilesets.Glyph
import things.LightSource
import things.Thing
import things.ThingHolder
import ui.panels.ConsolePanel
import util.*
import world.Level

@Serializable
sealed class Actor(
    open val glyph: Glyph,
    open val speed: Float
) : ThingHolder, LightSource {
    val xy = XY(0,0)  // position in current level
    var juice = 0f // How many turns am I owed?
    val queuedActions: MutableList<Action> = mutableListOf()

    @Transient var level: Level? = null

    override val contents = mutableListOf<Thing>()

    open fun moveTo(level: Level, x: Int, y: Int, fromLevel: Level?) {
        this.xy.x = x
        this.xy.y = y
        fromLevel?.onActorMovedFrom(this, x, y, level)
        level.onActorMovedTo(this, x, y)
    }

    // What's my divider for action durations?
    fun speed() = this.speed
    // How far in tiles can I see things?
    fun visualRange() = 24f

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
    open fun queue(action: Action) {
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
}
