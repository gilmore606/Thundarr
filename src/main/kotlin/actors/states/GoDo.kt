package actors.states

import actors.NPC
import actors.actions.Action
import util.XY
import world.path.Pather

class GoDo(
    val targetXY: XY,
    val targetAction: Action
    ) : State() {

    override fun toString() =  "GoDo (at $targetXY, do $targetAction)"

    override fun enter(npc: NPC) {
        Pather.subscribe(npc, npc, npc.visualRange())
    }

    override fun leave(npc: NPC) {
        Pather.unsubscribe(npc, npc)
    }

    override fun pickAction(npc: NPC): Action {
        if (npc.xy() == targetXY) {
            npc.popState()
            return targetAction
        } else {
            npc.stepToward(targetXY)?.also { return it }
        }
        return super.pickAction(npc)
    }

    override fun converseLines(npc: NPC) = listOf("No time to chat, I've got to go do something.")

}
