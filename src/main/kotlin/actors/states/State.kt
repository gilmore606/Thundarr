package actors.states

import actors.NPC
import actors.actions.Action
import actors.actions.Wait
import kotlinx.serialization.Serializable
import render.tilesets.Glyph

// A State represents a goal the NPC is trying to achieve.
@Serializable
sealed class State {
    open fun wantsToAct() = true

    open fun enter(npc: NPC) { }
    open fun leave(npc: NPC) { }
    open fun onRestore(npc: NPC) { }

    // Consider my situation and possibly change to a new state.
    open fun considerState(npc: NPC) { }

    open fun pickAction(npc: NPC): Action = Wait(1f)

    open fun drawStatusGlyphs(drawIt: (Glyph) -> Unit) { }

}
