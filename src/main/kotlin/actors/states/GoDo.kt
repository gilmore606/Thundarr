package actors.states

import actors.actors.NPC
import actors.actions.Action
import actors.actions.Say
import actors.actions.Wait
import kotlinx.serialization.Serializable
import util.Dice
import util.XY
import util.log
import path.Pather

@Serializable
class GoDo(
    val targetXY: XY,
    val targetAction: Action? = null
    ) : State() {

    var failedSteps = 0

    override fun toString() =  "GoDo (at $targetXY, do $targetAction)"

    override fun commentLine() = if (Dice.chance(0.6f)) listOf(
        "Just a moment, I need to take care of this.",
        "Hang on a minute.",
    ).random() else null

    override fun pickAction(npc: NPC): Action {
        if (npc.xy() == targetXY) {
            npc.popState()
            return targetAction ?: Wait(0.5f)
        } else if ((npc.level?.isWalkableAt(npc, targetXY.x, targetXY.y) == false) && npc.xy().isAdjacentTo(targetXY)) {
            npc.popState()
            return targetAction ?: Wait(0.5f)
        } else if (failedSteps > 4) {
            log.info("$npc $this gave up ($failedSteps failed steps)")
            npc.popState()
            return Say("Aww, forget it.")
        } else {
            npc.stepToward(targetXY)?.also {
                failedSteps = 0
                return it
            } ?: run { failedSteps++ }
        }
        return super.pickAction(npc)
    }
}
