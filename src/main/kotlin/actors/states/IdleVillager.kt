package actors.states

import actors.Actor
import actors.NPC
import actors.Villager
import actors.actions.*
import actors.actions.events.Event
import actors.actions.events.Knock
import kotlinx.serialization.Serializable
import things.Door
import things.SwitchablePlacedLight
import things.Thing
import util.*

@Serializable
class IdleVillager(
    val restTime: DayTime,
    val sleepTime: DayTime,
    val wakeTime: DayTime,
    val workTime: DayTime,
) : Idle() {
    val wanderChance = 0.4f
    val commentChance = 0.03f

    override fun toString() = "IdleVillager"

    override fun onEnter(npc: NPC) {
        if (npc is Villager) {
            npc.setTarget(npc.targetJob)
        }
    }

    override fun pickAction(npc: NPC): Action {
        if (npc is Villager) { npc.apply {
            entitiesNextToUs { it is Door && it.isOpen }.firstOrNull()?.also { door ->
                // Close the door behind us if we're leaving this area, or entering our home
                if (previousTargetJob.includesDoor(door as Door)) {
                    if ((targetJob != previousTargetJob) && !previousTargetJob.contains(npc)) {
                        return Use(Thing.UseTag.CLOSE, (door as Thing).getKey())
                    }
                } else if (homeJob.includesDoor(door)) {
                    if (targetJob == homeJob && homeJob.contains(npc)) {
                        return Use(Thing.UseTag.CLOSE, (door as Thing).getKey())
                    }
                }
            }
            if (targetJob.contains(npc)) {
                // We're here, do whatever!
                if (!isChild && Dice.chance(commentChance)) {
                    return Say(randomComment = true)
                }
                if (Dice.chance(wanderChance)) {
                    return wander(npc, wanderCheck(targetJob.rect))
                }
            } else {
                // Go where we should be!
                stepToward(targetJob.rect)?.also { return it } ?: run {
                    return wander(npc) { true }
                }
            }
        }}
        return super.pickAction(npc)
    }

    private fun wanderCheck(rect: Rect): (XY)->Boolean = { wanderXy ->
        rect.contains(wanderXy)
    }

    override fun considerState(npc: NPC) {
        if (npc is Villager) { npc.apply {
            if (App.gameTime.isBefore(wakeTime) || App.gameTime.isAfter(sleepTime)) {
                pushState(Sleeping(wakeTime))
                return
            } else if (App.gameTime.isAfter(restTime)) {
                if (targetJob != homeJob) {
                    say("Time to head home.")
                    setTarget(homeJob)
                }
            } else if (App.gameTime.isAfter(workTime)) {
                if (targetJob == homeJob) {
                    pickJob()
                } else if (App.gameTime.isAfter(nextJobChangeTime)) {
                    pickJob()
                }
            }

            entitiesSeen { it is SwitchablePlacedLight && !it.leaveLit() }.keys.firstOrNull()?.also { light ->
                if ((light as SwitchablePlacedLight).active && previousTargetJob.contains(light) && (targetJob != previousTargetJob) &&
                    previousTargetJob.contains(npc) && previousTargetJob.villagerCount(level()) <= 1) {
                    // Is it lit, and we're leaving, and we're here, and nobody else is here?  Extinguish it
                    pushState(GoDo(light.xy(), Use(Thing.UseTag.SWITCH_OFF, light.getKey())))
                } else if (!light.active && targetJob.contains(npc) && targetJob.contains(light)) {
                    // Is it out, and we're staying here?  Light it
                    pushState(GoDo(light.xy(), Use(Thing.UseTag.SWITCH_ON, light.getKey())))
                }
            }

        }}
        super.considerState(npc)
    }

    override fun witnessEvent(npc: NPC, culprit: Actor?, event: Event, location: XY) {
        if (npc is Villager) { npc.apply {
            if (event is Knock && culprit == null && homeJob.contains(npc)) {
                say("Who's that?")
                // Answer the door!
                pushState(GoDo(event.door.xy(), Use(Thing.UseTag.OPEN, event.door.getKey())))
            }
        }}
    }

}
