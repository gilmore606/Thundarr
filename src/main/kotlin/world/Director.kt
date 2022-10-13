package world

import actors.Actor
import actors.Player
import actors.WorldActor
import kotlinx.serialization.Serializable

// A delegate class for Level to manage actors in time and space.

@Serializable
class Director {

    val actors: MutableList<Actor> = mutableListOf(WorldActor())

    // Place actor into the level.
    fun add(actor: Actor, x: Int, y: Int) {
        attachActor(actor)
        actor.moveTo(x, y)
    }

    // Attach already-positioned actor in the level.  Used externally by loaded chunks.
    fun attachActor(actor: Actor) {
        actor.juice = 0f
        addOrdered(actor)
    }

    fun remove(actor: Actor) {
        actors.remove(actor)
    }

    // Insert actor into the queue in the correct order based on juice and speed.
    private fun addOrdered(actor: Actor) {
        var i = 0
        val actorSpeed = actor.speed()
        while (i < actors.size) {
            if (
                (actors[i].juice < actor.juice) ||
                (actors[i].juice == actor.juice && actor is Player) ||
                (actors[i].juice == actor.juice && actors[i].speed() < actorSpeed)
            ) {
                actors.add(i, actor)
                return
            }
            i++
        }
        actors.add(actor)
    }

    fun getPlayer(): Player = actors.firstOrNull { it is Player } as Player

    fun removeActorsInArea(x0: Int, x1: Int, y0: Int, y1: Int): Set<Actor> = actors.filter {
        it.real && it.xy.x >= x0 && it.xy.y >= y0 && it.xy.x <= x1 && it.xy.y <= y1
    }.map { remove(it) ; it }.toSet()

    // Execute actors' actions until it's the player's turn.
    fun runQueue(level: Level) {
        var done = false
        while (actors.isNotEmpty() && !done) {
            if (actors[0].juice > 0f || actors[0] is Player) {
                val actor = actors.removeAt(0)
                actor.nextAction()?.also { action ->
                    val duration = action.duration()
                    //log.debug("$actor (j ${actor.juice}) executes $action for $duration turns")

                    action.execute(actor, level)

                    when (actor) {
                        is WorldActor -> { actor.juice -= duration ; done = true } // Stop executing actions to let the renderer draw.
                        is Player -> { actors.forEach { it.juice += duration} } // Player actions give juice to everyone else.
                        else -> { actor.juice -= duration } // NPC actions cost them juice.
                    }
                } ?: if (actor is Player) {
                    // Player's turn, but has no queued actions, so we're done.
                    done = true
                } else {
                    // NPCs should always return an action, even if it's a wait.
                    throw RuntimeException("NPC $actor had no next action!")
                }
                // Put the actor back in queue, in position for their next turn.
                addOrdered(actor)
            } else {
                // If this actor has no juice, nobody does, so we're done.
                done = true
            }
        }
    }
}
