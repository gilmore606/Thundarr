package actors.actions.events

import actors.Actor
import util.XY
import world.level.Level

interface Event {

    enum class Sense { VISUAL, AUDIBLE }

    fun eventSenses(): Set<Sense> = setOf(Sense.VISUAL, Sense.AUDIBLE)

    fun broadcastEvent(level: Level, culprit: Actor?, location: XY) {
        level.director.actors.forEach { actor ->
            if (actor != culprit) {
                val senses = eventSenses()
                if ((senses.contains(Sense.VISUAL) && actor.canSee(location)) ||
                    (senses.contains(Sense.AUDIBLE) && actor.canHear(location, 1f))
                ) {
                    actor.witnessEvent(
                        if (actor.canSee(culprit)) culprit else null,
                        this, location
                    )
                }
            }
        }
    }
}
