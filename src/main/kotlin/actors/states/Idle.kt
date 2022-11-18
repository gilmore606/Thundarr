package actors.states

import actors.NPC
import kotlinx.serialization.Serializable
import util.log

@Serializable
class Idle : State() {

    override fun considerState(npc: NPC) {
        if (npc.isHostile() && npc.canSee(App.player)) {
            npc.changeState(npc.hostileResponseState(App.player.id))
        }
    }

}
