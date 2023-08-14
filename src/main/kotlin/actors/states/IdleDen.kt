package actors.states

import actors.actors.NPC
import actors.actions.Action
import actors.actions.Sleep
import actors.statuses.Status
import kotlinx.serialization.Serializable
import util.Dice
import path.Pather


@Serializable
class IdleDen(
    val wanderChance: Float,
    val wanderRadius: Int,
    val wanderChunkOnly: Boolean,
    val sleepHour: Float,
    val wakeHour: Float,
) : Idle() {
    override fun toString() = "IdleDen"

    // TODO: Change this to just pass the den in constructor
    override fun pickAction(npc: NPC): Action {
        if (shouldSleep(sleepHour, wakeHour)) {
            if (!npc.hasStatus(Status.Tag.ASLEEP)) {
                if (npc.den != null) {
                    if (npc.xy == npc.den?.xy()) {
                        npc.fallAsleep()
                        return Sleep()
                    } else {
                        npc.stepToward(npc.den!!.xy())?.also {
                            return it
                        }
                    }
                }
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
