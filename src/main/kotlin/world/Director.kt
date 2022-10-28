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

    private val temporals = mutableListOf<Temporal>()

    private var playerTimePassed = 0f

    fun attachActor(actor: Actor) {
        if (actor is Player && actor != App.player) {
            throw RuntimeException("Duplicate player attached!")
        } else if (actor in actors) {
            throw RuntimeException("Attaching already-attached actor!")
        }
        actor.level = this@Director.level
        actor.juice = 0f
        actors.add(actor)
    }

    fun detachActor(actor: Actor) {
        actors.remove(actor)
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
    // TODO: change to give juice to all active levels not just this one

    fun runQueue(level: Level) {
        while (true) {
            // find actor with highest juice
            var actor: Actor? = null
            var checkActor: Actor
            var unloadingActor: Actor? = null
            for (n in 0 .. actors.lastIndex) {
                checkActor = actors[n]
                if (checkActor.isUnloading) {
                    unloadingActor = checkActor
                } else if (checkActor.isActing()) {
                    if ((checkActor.juice > (actor?.juice ?: 0f)) ||
                        ((checkActor is Player) && (checkActor.juice == (actor?.juice ?: 0f))) ||
                        ((checkActor.juice == (actor?.juice ?: 0f)) && (checkActor.speed() > (actor?.speed() ?: 0f)))
                    ) {
                        actor = checkActor
                    }
                }
            }
            unloadingActor?.also { actors.remove(it) }
            if (actor == null || !actor.canAct()) return  // no one had juice, we're done

            // execute their action
            actor.nextAction()?.also { action ->
                val duration = action.duration()
                action.execute(actor, level)
                // pay the juice
                if (actor is Player) {  // player pays juice to all actors
                    actors.forEach {
                        if (it.isActing() && (it !is Player)) { it.juice += duration }
                    }
                    playerTimePassed += duration
                    if (playerTimePassed >= 1f) {
                        App.advanceTime(playerTimePassed)
                        playerTimePassed = 0f
                        return  // quit to let the renderer run
                    }
                } else {  // NPC spends juice
                    actor.juice -= duration
                }
            } ?: run {
                // they had juice, but no action
                if (actor is Player) return
                throw RuntimeException("NPC $actor had no next action!")
            }
        }
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
