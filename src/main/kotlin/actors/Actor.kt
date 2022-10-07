package actors

import actors.actions.Action
import util.Glyph
import util.XY

abstract class Actor(
    val glyph: Glyph,
    private val speed: Float
) {
    val xy = XY(0,0)  // position in current level

    private val queuedActions: MutableList<Action> = mutableListOf()

    // How many turns am I owed?
    var juice = 0f

    open fun moveTo(x: Int, y: Int) {
        this.xy.x = x
        this.xy.y = y
    }

    // What's my divider for action durations?
    fun speed() = this.speed

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
}
