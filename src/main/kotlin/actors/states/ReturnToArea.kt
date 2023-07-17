package actors.states

import actors.actors.NPC
import actors.actions.Action
import kotlinx.serialization.Serializable
import util.Rect
import path.Pather

@Serializable
class ReturnToArea(
    val area: Rect
) : State() {

    override fun toString() = "ReturnToArea ($area)"

    override fun onEnter(npc: NPC) {
        Pather.subscribe(npc, area, 40)
    }

    override fun onLeave(npc: NPC) {
        Pather.unsubscribe(npc, area)
    }

    override fun considerState(npc: NPC) {
        if (area.contains(npc.xy)) {
            npc.popState()
            return
        }
    }

    override fun pickAction(npc: NPC): Action {
        npc.stepToward(area)?.also { return it }
        return super.pickAction(npc)
    }
}
