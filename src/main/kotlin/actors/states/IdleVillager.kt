package actors.states

import actors.NPC
import actors.Villager
import actors.actions.Action
import actors.actions.Say
import actors.actions.Use
import kotlinx.serialization.Serializable
import things.Candlestick
import things.Thing
import util.Dice
import util.Rect
import util.XY

@Serializable
class IdleVillager(
    val restHour: Int,
    val sleepHour: Int,
    val wakeHour: Int,
    val workHour: Int,
) : Idle() {
    val scheduleMinute = Dice.zeroTil(50)
    val wanderChance = 0.4f
    val commentChance = 0.1f
    override fun pickAction(npc: NPC): Action {
        if (npc is Villager) {
            if (npc.targetArea.contains(npc.xy)) {
                // We're here, do whatever!
                if (Dice.chance(commentChance)) {
                    return Say(npc.targetArea.comments.random())
                }
                if (Dice.chance(wanderChance)) {
                    return wander(npc, wanderCheck(npc.targetArea.rect))
                }
            } else {
                // Go where we should be!
                npc.stepToward(npc.targetArea.rect)?.also { return it }
            }
        }
        return super.pickAction(npc)
    }

    private fun wanderCheck(rect: Rect): (XY)->Boolean = { wanderXy ->
        rect.contains(wanderXy)
    }

    override fun considerState(npc: NPC) {
        if (npc is Villager) {

            val h = App.gameTime.hour
            val m = App.gameTime.minute
            if (h >= restHour && m >= scheduleMinute || h <= wakeHour && m <= scheduleMinute) {
                if (npc.targetArea != npc.homeArea) {
                    npc.say("Time to head home.")
                    npc.setTarget(npc.homeArea)
                }
            } else if (h >= workHour && m >= scheduleMinute) {
                if (npc.targetArea == npc.homeArea) {
                    npc.village?.also { village ->
                        val newArea = village.workAreas.random()
                        npc.say("Time to go to the ${newArea.name}.")
                        npc.setTarget(village.workAreas.random())
                    }
                }
            }

            npc.entitiesSeen { it is Candlestick }.keys.firstOrNull()?.also { light ->
                if ((light as Candlestick).lit && !npc.targetArea.contains(npc.xy)) {
                    // Is it lit, and we're leaving?  Extinguish it
                    npc.pushState(GoDo(light.xy(), Use(Thing.UseTag.SWITCH_OFF, light, 0.5f,
                    light.uses()[Thing.UseTag.SWITCH_OFF]!!.toDo, light.xy().x, light.xy().y)))
                } else if (!(light as Candlestick).lit && npc.targetArea.contains(npc.xy)) {
                    // Is it out, and we're staying here?  Light it
                    npc.pushState(GoDo(light.xy(), Use(Thing.UseTag.SWITCH_ON, light, 0.5f,
                        light.uses()[Thing.UseTag.SWITCH_ON]!!.toDo, light.xy().x, light.xy().y)))
                }
            }

        }
        super.considerState(npc)
    }
}
