package actors.states

import actors.NPC
import actors.actions.Action
import actors.actions.Say
import actors.actions.Wait
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import util.Dice
import util.XY
import util.log
import world.path.Pather

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

    override fun onEnter(npc: NPC) {
        log.info("$npc GoDo subscribing pather for $targetXY (from ${npc.xy})")
        Pather.subscribe(npc, targetXY, npc.visualRange().toInt())
    }

    override fun onLeave(npc: NPC) {
        Pather.unsubscribe(npc, targetXY)
    }

    override fun pickAction(npc: NPC): Action {
        if (npc.xy() == targetXY) {
            npc.popState()
            return targetAction ?: Wait(0.5f)
        } else if (failedSteps > 4) {
            npc.popState()
            log.info("$this gave up GoDo for $targetAction")
            return Say("Aww, forget it.")
        } else {
            npc.stepToward(targetXY)?.also { failedSteps = 0; return it } ?: run { failedSteps++ }
        }
        return super.pickAction(npc)
    }
}
