package actors.states

import actors.actors.Actor
import actors.actors.NPC
import actors.actors.Villager
import actors.actions.*
import actors.actions.events.Event
import actors.statuses.Status
import kotlinx.serialization.Serializable
import things.GenericTorch
import things.SwitchablePlacedLight
import things.SwitchablePortableLight
import things.Thing
import util.DayTime
import util.XY
import util.hasOneWhere

@Serializable
class SleepingVillager(
    val wakeTime: DayTime
) : State() {
    val sleptTime = App.gameTime.dayTime()

    override fun toString() = "Sleeping (til $wakeTime))"

    override fun canSee() = false
    override fun allowsConversation() = false
    override fun idleBounce() = 0f

    override fun considerState(npc: NPC) {
        if (npc is Villager) {
            if (!npc.hasStatus(Status.Tag.ASLEEP)) {
                if (npc.targetJob != npc.homeJob) {
                    npc.setTarget(npc.homeJob)
                    npc.popState()
                    return
                }
                val awakeRoomies = npc.targetJob.villagers(npc.level).hasOneWhere { it.state !is SleepingVillager }
                if (!awakeRoomies) {
                    npc.entitiesSeen { it is SwitchablePlacedLight && !it.leaveLit() && it.active && npc.targetJob.contains(it) }.keys.firstOrNull()?.also { light ->
                        npc.pushState(GoDo(light.xy(), Use(Thing.UseTag.SWITCH_OFF, (light as Thing).getKey())))
                        return
                    }
                    npc.entitiesSeen { it is GenericTorch && it.active && npc.targetJob.contains(it) }.keys.firstOrNull()?.also { light ->
                        npc.pushState(GoDo(light.xy(), Use(Thing.UseTag.SWITCH_OFF, (light as Thing).getKey())))
                    }
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

    override fun witnessEvent(npc: NPC, culprit: Actor?, event: Event, location: XY) {
        when (event) {
            is Shout -> {
                // TODO: senses check?
                npc.popState()
                return
            }
        }
    }

    override fun receiveAggression(npc: NPC, attacker: Actor) {
        npc.popState()
    }

    private fun shouldBeAsleep(): Boolean {
        if (App.gameTime.isBefore(wakeTime)) {
            return true
        }
        if (!App.gameTime.isBefore(sleptTime) && sleptTime.isAfter(wakeTime)) {
            return true
        }
        return false
    }
}
