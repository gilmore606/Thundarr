package actors.states

import actors.NPC
import actors.Villager
import actors.actions.*
import actors.statuses.Status
import things.Candlestick
import things.Door
import things.Thing

class Sleeping(
    val wakeHour: Int,
    val wakeMinute: Int,
) : State() {
    val sleptHour = App.gameTime.hour
    val sleptMinute = App.gameTime.minute

    override fun toString() = "Sleeping (til $wakeHour:$wakeMinute)"

    override fun considerState(npc: NPC) {
        if (npc is Villager) {
            if (!npc.hasStatus(Status.Tag.ASLEEP)) {
                npc.entitiesSeen { it is Candlestick && it.lit && npc.targetArea.contains(it.xy()) }.keys.firstOrNull()?.also { light ->
                    npc.pushState(GoDo(light.xy(), Use(Thing.UseTag.SWITCH_OFF, light as Candlestick)))
                    return
                }
                if (npc.xy() != npc.bedLocation) {
                    npc.pushState(GoDo(npc.bedLocation, Say(":yawns.")))
                    return
                }
                if (shouldBeAsleep()) {
                    npc.fallAsleep()
                }
            } else if (!shouldBeAsleep()) {
                npc.wakeFromSleep()
                npc.popState()
            }
        }
    }

    override fun pickAction(npc: NPC): Action {
        if (npc.hasStatus(Status.Tag.ASLEEP)) return Sleep()
        return Wait(0.5f)
    }

    private fun shouldBeAsleep(): Boolean {
        if (App.gameTime.isBefore(wakeHour, wakeMinute)) {
            return true
        }
        if (!App.gameTime.isBefore(sleptHour, sleptMinute) && sleptHour >= wakeHour && sleptMinute > wakeMinute) {
            return true
        }
        return false
    }

    override fun converseLines(npc: NPC) = listOf("Zzz...")
}
