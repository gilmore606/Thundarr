package actors.states

import actors.actors.NPC
import actors.actions.Action
import actors.actions.Sleep
import actors.statuses.Status
import kotlinx.serialization.Serializable
import util.Dice
import path.Pather
import util.manhattanDistance


@Serializable
class IdleDen(
    val wanderChance: Float,
    val wanderRadius: Int,
    val wanderChunkOnly: Boolean,
    val sleepHour: Float? = null,
    val wakeHour: Float? = null,
) : Idle() {
    override fun toString() = "IdleDen"

    override fun considerState(npc: NPC) {
        npc.den?.xy()?.also { denXY ->
            if (manhattanDistance(npc.xy, denXY) > wanderRadius) {
                npc.pushState(GoDo(denXY))
                return
            }
        }
        super.considerState(npc)
    }

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
