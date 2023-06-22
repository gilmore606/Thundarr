package actors.states

import actors.NPC
import actors.actions.Action
import actors.actions.Say
import kotlinx.serialization.Serializable
import util.Rect
import world.path.Pather

@Serializable
class ReturnToArea(
    val area: Rect
) : State() {

    override fun toString() = "ReturnToArea ($area)"

    override fun enter(npc: NPC) {
        Pather.subscribe(npc, area, 40)
    }

    override fun leave(npc: NPC) {
        Pather.unsubscribe(npc, area)
    }

    override fun considerState(npc: NPC) {
        if (area.contains(npc.xy)) {
            npc.popState()
            return
        }
        super.considerState(npc)
    }

    override fun pickAction(npc: NPC): Action {
        npc.stepToward(area)?.also { return it }
        return super.pickAction(npc)
    }
}
