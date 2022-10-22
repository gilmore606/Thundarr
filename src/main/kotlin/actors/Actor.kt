package actors

import actors.actions.Action
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Thing
import things.ThingHolder
import ui.panels.ConsolePanel
import util.XY
import util.aOrAn
import world.Level

@Serializable
sealed class Actor(
    open val glyph: Glyph,
    open val speed: Float
) : ThingHolder {
    val xy = XY(0,0)  // position in current level

    val queuedActions: MutableList<Action> = mutableListOf()

    // How many turns am I owed?
    var juice = 0f

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

    override val contents = mutableListOf<Thing>()
    override fun remove(thing: Thing) { contents.remove(thing) }
    override fun add(thing: Thing) { contents.add(thing) }

}
