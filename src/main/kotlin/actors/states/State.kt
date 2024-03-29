package actors.states

import actors.actors.Actor
import actors.actors.NPC
import actors.actions.Action
import actors.actions.Attack
import actors.actions.Wait
import actors.actions.events.Event
import kotlinx.serialization.Serializable
import render.Screen
import render.tilesets.Glyph
import util.XY
import util.distanceBetween
import util.log

// A State represents a goal the NPC is trying to achieve.
@Serializable
sealed class State {
    open fun wantsToAct() = true

    var enterTime: Double = 0.0

    fun enter(npc: NPC) {
        enterTime = App.time
        onEnter(npc)
    }

    fun leave(npc: NPC) {
        onLeave(npc)
    }

    fun elapsed() = App.time - enterTime

    protected open fun onEnter(npc: NPC) { }
    protected open fun onLeave(npc: NPC) { }
    open fun onRestore(npc: NPC) { }

    // Consider my situation and possibly change to a new state.
    open fun considerState(npc: NPC) {
        npc.apply {
            entitiesSeen {
                it is Actor && (opinionOf(it) == NPC.Opinion.HATE && distanceBetween(it.xy, xy) <= aggroRange())
            }.keys.toList().firstOrNull()
                ?.also { enemy ->
                    hostileResponseState(enemy as Actor)?.also { pushState(it) }
                }
        }
    }

    open fun pickAction(npc: NPC): Action = Wait(1f)

    open fun witnessEvent(npc: NPC, culprit: Actor?, event: Event, location: XY) {
        // Defend our species
        npc.apply {
            if (state !is Attacking && state !is Fleeing && event is Attack && defendSpecies()) {
                getActor(event.targetID)?.also { target ->
                    if (target is NPC && target.tag in speciesBrothers()) {
                        culprit?.also { culprit ->
                            hostileResponseState(culprit)?.also { pushState(it) }
                        }
                    }
                }
            }
        }
    }

    open fun receiveAggression(npc: NPC, attacker: Actor) { }

    open fun drawStatusGlyph(drawIt: (Glyph) -> Unit): Boolean { return false }

    open fun allowsConversation(): Boolean = true
    open fun commentLine(): String? = null

    open fun canSee() = true

    open fun idleBounceMs() = 750
    open fun idleBounce() = -0.04f
    open fun animOffsetY(npc: NPC): Float {
        if ((Screen.timeMs + npc.idleBounceOffset) % idleBounceMs() < idleBounceMs() / 2) {
            return idleBounce()
        } else return 0f
    }

}
