package actors.states

import actors.Actor
import actors.NPC
import actors.Villager
import actors.actions.*
import actors.actions.events.Event
import actors.actions.events.Knock
import kotlinx.serialization.Serializable
import things.Door
import things.SwitchableLight
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
    val commentChance = 0.05f

    override fun toString() = "IdleVillager"

    override fun onEnter(npc: NPC) {
        if (npc is Villager) {
            npc.setTarget(npc.targetArea)
        }
    }

    override fun pickAction(npc: NPC): Action {
        if (npc is Villager) {
            npc.entitiesNextToUs { it is Door && it.isOpen }.firstOrNull()?.also { door ->
                // Close the door behind us if we're leaving this area, or entering our home
                if (npc.previousTargetArea.includesDoor(door as Door)) {
                    if ((npc.targetArea != npc.previousTargetArea) && !npc.previousTargetArea.contains(npc.xy())) {
                        return Use(Thing.UseTag.CLOSE, (door as Thing).getKey())
                    }
                } else if (npc.homeArea.includesDoor(door)) {
                    if (npc.targetArea == npc.homeArea && npc.homeArea.contains(npc.xy())) {
                        return Use(Thing.UseTag.CLOSE, (door as Thing).getKey())
                    }
                }
            }
            if (npc.targetArea.contains(npc.xy)) {
                // We're here, do whatever!
                if (!npc.isChild && Dice.chance(commentChance)) {
                    return Say(npc.targetArea.comments.random())
                }
                if (Dice.chance(wanderChance)) {
                    return wander(npc, wanderCheck(npc.targetArea.rect))
                }
            } else {
                // Go where we should be!
                npc.stepToward(npc.targetArea.rect)?.also { return it } ?: run {
                    return wander(npc) { true }
                }
            }
        }
        return super.pickAction(npc)
    }

    private fun wanderCheck(rect: Rect): (XY)->Boolean = { wanderXy ->
        rect.contains(wanderXy)
    }

    override fun considerState(npc: NPC) {
        if (npc is Villager) {
            if (npc.targetArea == Villager.defaultArea) {
                npc.setTarget(npc.homeArea)
            }
            if (App.gameTime.isBefore(wakeTime) || App.gameTime.isAfter(sleepTime)) {
                npc.pushState(Sleeping(wakeTime))
                return
            } else if (App.gameTime.isAfter(restTime)) {
                if (npc.targetArea != npc.homeArea) {
                    npc.say("Time to head home.")
                    npc.setTarget(npc.homeArea)
                }
            } else if (App.gameTime.isAfter(workTime)) {
                if (npc.targetArea == npc.homeArea) {
                    npc.pickJob()
                } else if (App.gameTime.isAfter(npc.nextJobChangeTime)) {
                    npc.pickJob()
                }
            }

            npc.entitiesSeen { it is SwitchableLight && !it.leaveLit() }.keys.firstOrNull()?.also { light ->
                if ((light as SwitchableLight).lit && npc.previousTargetArea.contains(light.xy()) && (npc.targetArea != npc.previousTargetArea) &&
                    npc.previousTargetArea.contains(npc.xy()) && npc.previousTargetArea.villagerCount(npc.level()) <= 1) {
                    // Is it lit, and we're leaving, and we're here, and nobody else is here?  Extinguish it
                    npc.pushState(GoDo(light.xy(), Use(Thing.UseTag.SWITCH_OFF, light.getKey())))
                } else if (!(light).lit && npc.targetArea.contains(npc.xy) && npc.targetArea.contains(light.xy())) {
                    // Is it out, and we're staying here?  Light it
                    npc.pushState(GoDo(light.xy(), Use(Thing.UseTag.SWITCH_ON, light.getKey())))
                }
            }

        }
        super.considerState(npc)
    }

    override fun witnessEvent(npc: NPC, culprit: Actor?, event: Event, location: XY) {
        if (npc is Villager) {
            if (event is Knock && culprit == null && npc.homeArea.contains(npc.xy)) {
                npc.say("Who's that?")
                // Answer the door!
                npc.pushState(GoDo(event.door.xy(), Use(Thing.UseTag.OPEN, event.door.getKey())))
            }
        }
    }

    override fun converseLines(npc: NPC): List<String>? {
        if (npc is Villager) {
            if (npc.isChild) {
                return listOf(
                    "You look dumb.", "Your face is a butt.", "You smell like a butt!", "Why is your head weird?", "You're funny."
                )
            }
            if (!npc.targetArea.contains(npc.xy())) {
                return listOf(
                    "No time to chat, I've got to get " +
                            (if (npc.targetArea == npc.homeArea) "home" else ("to the " + npc.targetArea.name)) +
                            "."
                )
            }
            return npc.targetArea.comments.toList()
        }
        return null
    }
}
