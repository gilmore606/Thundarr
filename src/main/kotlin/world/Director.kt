package world

import actors.Actor
import actors.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import util.log
import java.lang.RuntimeException

// A delegate class for Level to manage actors in time and space.

class Director(
    val level: Level
){

    val actors: MutableList<Actor> = mutableListOf()
    private var running = false

    // Place actor into the level.
    fun add(actor: Actor, x: Int, y: Int) {
        actor.juice = 0f
        addOrdered(actor)
        actor.moveTo(x, y)
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


    // Execute actors' actions until it's the player's turn.
    fun runQueue() {
        if (running) return
        CoroutineScope(Dispatchers.Default).launch {
            running = true
            var needInput = false
            while (actors.isNotEmpty() && !needInput) {
                // Pull the next actor out of queue.
                val actor = actors.removeAt(0)
                if (actor.juice > 0f || actor is Player) {
                    actor.nextAction()?.also { action ->
                        val duration = action.duration()
                        //log.debug("$actor (j ${actor.juice}) executes $action for $duration turns")

                        action.execute(actor, level)

                        if (actor is Player) {
                            // Player actions give juice to everyone else.
                            actors.forEach { it.juice += duration }
                        } else {
                            // NPC actions cost them juice.
                            actor.juice -= duration
                        }
                    } ?: if (actor is Player) {
                        // Player's turn, but has no queued actions, so we're done.
                        needInput = true
                    } else {
                        // NPCs should always return an action, even if it's a wait.
                        throw RuntimeException("NPC $actor had no next action!")
                    }
                } else {
                    // If this actor has no juice, nobody does, so we're done.
                    needInput = true
                }
                // Put the actor back in queue, in position for their next turn.
                addOrdered(actor)

                Thread.sleep(5)
            }
            running = false
        }

    }
}
