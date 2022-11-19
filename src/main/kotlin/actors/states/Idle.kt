package actors.states

import actors.NPC
import audio.Speaker
import kotlinx.serialization.Serializable
import ui.panels.Console

@Serializable
class Idle : State() {

    override fun considerState(npc: NPC) {
        val canSeePlayer = npc.canSee(App.player)
        if (canSeePlayer && !npc.metPlayer) {
            npc.meetPlayerMsg()?.also {
                Console.say(it)
                npc.talkSound(App.player)?.also { Speaker.world(it, source = npc.xy())}
            }
            npc.metPlayer = true
        }

        if (npc.isHostile() && canSeePlayer) {
            npc.changeState(npc.hostileResponseState(App.player.id))
        }
    }

}
