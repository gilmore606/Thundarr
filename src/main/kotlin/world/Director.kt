package world

import actors.actors.Actor
import actors.actors.NPC
import actors.actors.Player
import actors.states.Hibernated
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import things.Temporal
import util.XY
import util.distanceBetween
import util.filterOut
import world.level.Level
import path.Pather
import util.forceMainThread
import world.persist.LevelKeeper

// A delegate class for Level to manage actors in time and space.

class Director(val level: Level) {

    companion object {
        const val millisPerRun = 8L
    }

    val actors = mutableListOf<Actor>()
    private val temporals = mutableListOf<Temporal>()

    private var playerTimePassed = 0f
    var actorsLocked = false
    var actionsPending = false

    fun attachActor(actor: Actor) {
        if (actor is Player && actor != App.player) {
            throw RuntimeException("Duplicate player attached!")
        } else if (actor in actors) {
            throw RuntimeException("Attaching already-attached actor!")
        }
        actor.level = this@Director.level
        actor.juice = 0f
        addActor(actor)
    }

    private fun addActor(new: Actor) {
        forceMainThread {
            actors.add(new)
            sortActors()
        }
    }

    private fun removeActor(old: Actor) {
        forceMainThread {
            actors.remove(old)
            sortActors()
        }
    }

    private fun sortActors() {
        actors.sortBy { it.xy.y }
    }

    fun detachActor(actor: Actor) {
        removeActor(actor)
    }

    fun getActor(id: String) = actors.firstOrNull { it.id == id }

    fun unloadActorsFromArea(x0: Int, y0: Int, x1: Int, y1: Int): Set<Actor> {
        val unloads = actors.filter { it.xy.x >= x0 && it.xy.y >= y0 && it.xy.x <= x1 && it.xy.y <= y1 }
        val unloadSet = mutableSetOf<Actor>()
        for (actor in unloads) {
            if (actor !is Player) {
                detachActor(actor)
                actor.isUnloading = true
                Pather.saveActorMaps(actor)
                unloadSet.add(actor)
            }
        }
        return unloadSet
    }

    fun wakeNPCsNear(xy: XY) {
        actors.forEach {
            if (it is NPC) {
                if (it.state is Hibernated &&
                    it.xy.x >= xy.x - it.unhibernateRadius && it.xy.y >= xy.y - it.unhibernateRadius &&
                    it.xy.x <= xy.x + it.unhibernateRadius && it.xy.y <= xy.y + it.unhibernateRadius &&
                    distanceBetween(xy.x, xy.y, it.xy.x, it.xy.y) < it.unhibernateRadius) {
                    it.changeState(it.idleState())
                }
            }
        }
    }

    fun actorAt(x: Int, y: Int): Actor? {
        for (i in 0 until actors.size) {
            if (i < actors.size && actors[i].xy.x == x && actors[i].xy.y == y) return actors[i]
        }
        return null
    }

    // Execute actors' actions until it's the player's turn.
    fun runQueue(level: Level) {
        actionsPending = true
        val cutoffTime = System.currentTimeMillis() + millisPerRun
        while (System.currentTimeMillis() < cutoffTime) {
            if (level.hasBlockingSpark()) return
            // find actor with highest juice
            var actor: Actor? = null
            var checkActor: Actor
            var unloadingActor: Actor? = null
            for (n in 0..actors.lastIndex) {
                checkActor = actors[n]
                if (checkActor.isUnloading) {
                    unloadingActor = checkActor
                } else if (checkActor.wantsToAct()) {
                    if ((checkActor.juice > (actor?.juice ?: 0f)) ||
                        ((checkActor is Player) && (checkActor.juice == (actor?.juice ?: 0f))) ||
                        ((checkActor.juice > 0f) && (checkActor.juice == (actor?.juice
                            ?: 0f)) && (checkActor.actionSpeed() < (actor?.actionSpeed() ?: 0f)))
                    ) {
                        actor = checkActor
                    }
                }
            }
            unloadingActor?.also { removeActor(it) }
            if (actor == null || !actor.hasActionJuice()) {  // no one had juice, we're done
                triggerAdvanceTime()
                actionsPending = false
                return
            }
            // execute their action
            actor.nextAction()?.also { action ->
                val duration = action.durationFor(actor)

                actor.doAction(action)

                // pay the juice
                if (actor is Player) {  // player pays juice to all actors
                    LevelKeeper.advanceJuice(duration)
                    playerTimePassed += duration
                    if (playerTimePassed >= 1f) {
                        triggerAdvanceTime()
                        actionsPending = false
                        return  // quit to let the renderer run
                    }
                } else {  // NPC spends juice
                    actor.juice -= duration
                }
            } ?: run {
                // they had juice, but no action
                if (actor is Player) {
                    triggerAdvanceTime()
                    actionsPending = false
                    return
                }
                throw RuntimeException("NPC $actor had no next action!")
            }
        }
    }

    private fun triggerAdvanceTime() {
        if (playerTimePassed > 0f) {
            LevelKeeper.advanceTime(playerTimePassed * level.timeScale())
            playerTimePassed = 0f
        }
    }

    fun advanceJuice(juice: Float) {
        actors.forEach { if (it.wantsToAct() && (it !is Player)) { it.juice += juice } }
    }

    fun advanceTime(delta: Float) {
        actorsLocked = true
        sortActors()
        actorsLocked = false
        actors.filterOut({ it.level != level }) { it.advanceTime(delta) }
        temporals.filterOut({ it.temporalDone() }) { it.advanceTime(delta) }
    }

    fun linkTemporal(temporal: Temporal) {
        temporals.add(temporal)
    }

    fun unlinkTemporal(temporal: Temporal) {
        temporals.remove(temporal)
    }
}
