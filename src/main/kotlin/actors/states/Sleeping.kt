package actors.states

import actors.actors.Actor
import actors.actors.NPC
import actors.actors.Villager
import actors.actions.*
import actors.actions.events.Event
import actors.statuses.Status
import kotlinx.serialization.Serializable
import things.SwitchablePlacedLight
import things.Thing
import util.DayTime
import util.XY

@Serializable
class Sleeping(
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
                npc.entitiesSeen { it is SwitchablePlacedLight && !it.leaveLit() && it.active && npc.targetJob.contains(it) }.keys.firstOrNull()?.also { light ->
                    var awakeRoomies = false
                    npc.targetJob.villagers(npc.level).forEach { villager ->
                        if (villager.state !is Sleeping) awakeRoomies = true
                    }
                    if (!awakeRoomies) {
                        npc.pushState(GoDo(light.xy(), Use(Thing.UseTag.SWITCH_OFF, (light as Thing).getKey())))
                        return
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
