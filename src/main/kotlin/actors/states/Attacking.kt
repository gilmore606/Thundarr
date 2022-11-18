package actors.states

import actors.NPC
import actors.actions.Action
import actors.actions.Attack
import kotlinx.serialization.Serializable
import util.XY

@Serializable
class Attacking(
    val targetId: String
) : State() {

    override fun considerState(npc: NPC) {
        if (!npc.canSee(npc.getActor(targetId))) {
            if (npc.willSeek()) {
                npc.changeState(Seeking(targetId))
            } else {
                npc.changeState(Idle())
            }
        }
    }

    override fun pickAction(npc: NPC): Action {
        npc.getActor(targetId)?.also { target ->
            if (npc.entitiesNextToUs().contains(target)) {
                return Attack(target, XY(target.xy.x - npc.xy.x, target.xy.y - npc.xy.y))
            } else {
                npc.stepToward(target)?.also { return it }
            }
        }
        return super.pickAction(npc)
    }
}
