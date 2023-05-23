package actors.states

import actors.NPC
import actors.actions.Action
import actors.actions.Sleep
import actors.statuses.Status
import kotlinx.serialization.Serializable
import util.Dice
import util.log
import world.path.Pather


@Serializable
class IdleDen(
    val wanderChance: Float,
    val wanderRadius: Int,
    val wanderChunkOnly: Boolean,
    val sleepHour: Float,
    val wakeHour: Float,
) : Idle() {
    override fun pickAction(npc: NPC): Action {
        npc.den?.also { Pather.subscribe(npc, it, wanderRadius.toFloat()) }

        if (shouldSleep(sleepHour, wakeHour)) {
            if (!npc.hasStatus(Status.Tag.ASLEEP)) {
                if (npc.den != null) {
                    if (npc.xy == npc.den?.xy()) {
                        npc.fallAsleep()
                        return Sleep()
                    } else {
                        npc.stepToward(npc.den!!)?.also {
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
            return wander(npc, wanderCheck(wanderRadius, wanderChunkOnly, npc))
        }

        return super.pickAction(npc)
    }
}
