package actors.states

import actors.actors.NPC
import actors.actions.Action
import actors.actions.Sleep
import actors.statuses.Status
import kotlinx.serialization.Serializable
import util.Dice


@Serializable
class IdleHerd(
    val wanderChance: Float,
    val wanderRadius: Int,
    val wanderChunkOnly: Boolean,
    val sleepHour: Float,
    val wakeHour: Float,
) : Idle() {
    override fun toString() = "IdleHerd"

    override fun pickAction(npc: NPC): Action {
        if (shouldSleep(sleepHour, wakeHour)) {
            if (!npc.hasStatus(Status.Tag.ASLEEP)) {
                npc.fallAsleep()
                return Sleep()
            }
        } else {
            if (npc.hasStatus(Status.Tag.ASLEEP) && hoursFromSleep(sleepHour, wakeHour) > 2) {
                npc.wakeFromSleep()
            }
        }

        if (Dice.chance(wanderChance)) {
            return wander(npc, wanderCheck(wanderRadius, wanderChunkOnly, npc, npc.den?.xy()))
        }

        return super.pickAction(npc)
    }
}
