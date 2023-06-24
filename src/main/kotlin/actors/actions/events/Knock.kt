package actors.actions.events

import things.Door

class Knock(
    val door: Door
) : Event {
    override fun toString() = "knock"
    override fun eventSenses() = setOf(Event.Sense.AUDIBLE)
}
