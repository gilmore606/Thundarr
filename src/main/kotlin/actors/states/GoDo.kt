package actors.states

import actors.NPC
import actors.actions.Action
import actors.actions.Say
import actors.actions.Wait
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import util.XY
import world.path.Pather

@Serializable
class GoDo(
    val targetXY: XY,
    val targetAction: Action? = null
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
            return targetAction ?: Wait(0.5f)
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
