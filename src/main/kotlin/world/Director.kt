package world

import actors.Actor
import actors.NPC
import actors.Player
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import things.Temporal
import util.XY
import util.distanceBetween
import util.log

// A delegate class for Level to manage actors in time and space.

class Director(val level: Level) {

    val actors = mutableListOf<Actor>()
    private val actorQueue = mutableListOf<Actor>()
    private val nonActorQueue = mutableListOf<Actor>()

    private val temporals = mutableListOf<Temporal>()

    private var playerTimePassed = 0f

    private fun MutableList<Actor>.addOnce(actor: Actor) { if (!actor.isUnloading && !contains(actor)) add(actor) }
    private fun MutableList<Actor>.addOnce(i: Int, actor: Actor) { if (!actor.isUnloading && !contains(actor)) add(i, actor) }

    fun attachActor(actor: Actor) {
        KtxAsync.launch {
            if (actor is Player && actor != App.player) {
                throw RuntimeException("Duplicate player attached!")
            } else if (actor in actors) {
                throw RuntimeException("Attaching already-attached actor!")
            }
            actor.level = this@Director.level
            actor.juice = 0f
            addOrdered(actor, actors)
        }
    }

    fun detachActor(actor: Actor) {
        KtxAsync.launch {
            actors.remove(actor)
        }
    }

    // Insert actor into the queue in the correct order based on juice and speed.
    private fun addOrdered(actor: Actor, toQueue: MutableList<Actor>) {
        var i = 0
        val actorSpeed = actor.speed()
        while (i < toQueue.size) {
            if (
                (toQueue[i].juice < actor.juice) ||
                (toQueue[i].juice == actor.juice && actor is Player) ||
                (toQueue[i].juice == actor.juice && actors[i].speed() < actorSpeed)
            ) {
                toQueue.addOnce(i, actor)
                return
            }
            i++
        }
        toQueue.addOnce(actor)
    }

    fun unloadActorsFromArea(x0: Int, y0: Int, x1: Int, y1: Int): Set<Actor> {
        val unloads = actors.filter { it.xy.x >= x0 && it.xy.y >= y0 && it.xy.x <= x1 && it.xy.y <= y1 }
        val unloadSet = mutableSetOf<Actor>()
        for (actor in unloads) {
            if (actor !is Player) {
                detachActor(actor)
                actor.isUnloading = true
                unloadSet.add(actor)
            }
        }
        return unloadSet
    }

    fun wakeNPCsNear(xy: XY) {
        actors.forEach {
            if (it is NPC) {
                if (it.awareness == NPC.Awareness.HIBERNATED &&
                    it.xy.x >= xy.x - it.awareRadius && it.xy.y >= xy.y - it.awareRadius &&
                    it.xy.x <= xy.x + it.awareRadius && it.xy.y <= xy.y + it.awareRadius &&
                    distanceBetween(xy.x, xy.y, it.xy.x, it.xy.y) < it.awareRadius) {
                    it.unHibernate()
                }
            }
        }
    }

    // Execute actors' actions until it's the player's turn.
    fun runQueue(level: Level) {

        var done = false
        actorQueue.clear()
        nonActorQueue.clear()
        actors.forEach { if (it.isActing()) actorQueue.add(it) else nonActorQueue.add(it) }

        while (actorQueue.isNotEmpty() && !done) {
            if (actorQueue[0].canAct()) {
                val actor = actorQueue.removeAt(0)
                actor.nextAction()?.also { action ->

                    val duration = action.duration()
                    action.execute(actor, level)

                    when (actor) {
                        is Player -> {
                            actorQueue.forEach { if (it !is Player) it.juice += duration} // Player actions give juice to everyone else.
                            playerTimePassed += duration
                            if (playerTimePassed >= 1f) {
                                App.advanceTime(playerTimePassed)
                                playerTimePassed = 0f
                                done = true
                            }
                        }
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
                addOrdered(actor, actorQueue)
            } else {
                // If this actor has no juice, nobody does, so we're done.
                done = true
            }
        }

        actors.clear()
        actorQueue.forEach { actors.addOnce(it) }
        nonActorQueue.forEach { actors.addOnce(it) }
    }

    // Advance world time for all actors.
    fun advanceTime(delta: Float) {
        actors.forEach { it.advanceTime(delta) }
        temporals.forEach { it.advanceTime(delta) }
    }

    fun linkTemporal(temporal: Temporal) {
        temporals.add(temporal)
    }

    fun unlinkTemporal(temporal: Temporal) {
        temporals.remove(temporal)
    }
}
