package actors.states

import actors.NPC
import actors.actions.Action
import actors.actions.Say
import kotlinx.serialization.Serializable
import util.XY
import world.path.Pather

class GoDo(
    val targetXY: XY,
    val targetAction: Action
    ) : State() {

    var failedSteps = 0

    override fun toString() =  "GoDo (at $targetXY, do $targetAction)"

    override fun enter(npc: NPC) {
        Pather.subscribe(npc, targetXY, npc.visualRange().toInt())
    }

    override fun leave(npc: NPC) {
        Pather.unsubscribe(npc, targetXY)
    }

    override fun pickAction(npc: NPC): Action {
        if (npc.xy() == targetXY) {
            npc.popState()
            return targetAction
        } else if (failedSteps > 4) {
            npc.popState()
            return Say("Aww, forget it.")
        } else {
            npc.stepToward(targetXY)?.also { failedSteps = 0; return it } ?: run { failedSteps++ }
        }
        return super.pickAction(npc)
    }

    override fun converseLines(npc: NPC) = listOf("No time to chat, I've got to go do something.")

}
