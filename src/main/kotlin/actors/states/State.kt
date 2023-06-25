package actors.states

import actors.Actor
import actors.NPC
import actors.actions.Action
import actors.actions.Wait
import actors.actions.events.Event
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.XY

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
    open fun considerState(npc: NPC) { }

    open fun pickAction(npc: NPC): Action = Wait(1f)

    open fun witnessEvent(npc: NPC, culprit: Actor?, event: Event, location: XY) { }

    open fun receiveAggression(npc: NPC, attacker: Actor) { }

    open fun drawStatusGlyphs(drawIt: (Glyph) -> Unit) { }

    open fun converseLines(npc: NPC): List<String>? = null

}
