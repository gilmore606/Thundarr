package actors.states

import App
import actors.actors.NPC
import actors.actions.Action
import actors.actions.Move
import actors.actions.Wait
import kotlinx.serialization.Serializable
import util.*

@Serializable
sealed class Idle : State() {

    override fun idleBounceMs() = 1000

    protected fun wanderCheck(wanderRadius: Int, wanderChunkOnly: Boolean, npc: NPC): (XY)->Boolean = { wanderXy ->
        val den = npc.den
        if (den != null) {
            val denXy = den.xy()
            if (wanderChunkOnly && den.chunk() != npc.level?.chunkAt(wanderXy.x, wanderXy.y)) {
                false
            } else if (wanderRadius > 0 && manhattanDistance(denXy, wanderXy) > wanderRadius) {
                false
            } else {
                true
            }
        } else true
    }

    override fun considerState(npc: NPC) {
        npc.apply {
            // TODO: replace this junk with something robust
            val canSeePlayer = canSee(App.player)
            if (canSeePlayer && !metPlayer) {
                meetPlayerMsg()?.also { say(it) }
                metPlayer = true
            }
        }
        super.considerState(npc)
    }

    protected fun wander(npc: NPC, isOK: ((XY)->Boolean) = { true }): Action {
        npc.apply { level?.also { level ->
            placeMemory["lastWander"] ?: run { placeMemory["lastWander"] = XY(0,0) }
            placeMemory["lastWander2"] ?: run { placeMemory["lastWander2"] = XY(0,0) }
            val dirs = DIRECTIONS.toMutableList()
            while (dirs.isNotEmpty()) {
                val dir = dirs.removeAt(Dice.zeroTil(dirs.size))
                val dest = xy + dir
                if (level.isWalkableFrom(npc, xy, dir) && isOK(dest)) {
                    if (placeMemory["lastWander"] != dest && placeMemory["lastWander2"] != dest) {
                        placeMemory["lastWander2"]!!.setTo(placeMemory["lastWander"]!!)
                        placeMemory["lastWander"]!!.setTo(dest)
                        return Move(dir)
                    }
                }
            }
            placeMemory["lastWander"]!!.setTo(0,0)
            placeMemory["lastWander2"]!!.setTo(0,0)
        }}
        return Wait(1f)
    }

    fun shouldSleep(sleepHour: Float, wakeHour: Float): Boolean {
        val hoursFromSleep = hoursFromSleep(sleepHour, wakeHour)
        return if (hoursFromSleep <= -1f) true
        else if (hoursFromSleep <= 0f) Dice.chance(0.7f)
        else if (hoursFromSleep <= 1f) Dice.chance(0.2f)
        else false
    }

    fun hoursFromSleep(sleepHour: Float, wakeHour: Float): Float {
        val hour = App.gameTime.hour
        return if (sleepHour > wakeHour) {
            if (hour < wakeHour) hour - wakeHour
            else sleepHour - hour
        } else {
            if (hour < sleepHour) hour - sleepHour
            else wakeHour - hour
        }
    }
}

@Serializable
class IdleDoNothing : Idle() {
    override fun toString() = "IdleDoNothing"
}

@Serializable
class IdleInRoom : Idle() {
    override fun toString() = "IdleInRoom"
    override fun pickAction(npc: NPC): Action {
        if (Dice.chance(0.5f)) return wander(npc) {
            npc.placeMemory["myRoom0"]?.let { room0 ->
                npc.placeMemory["myRoom1"]?.let { room1 ->
                    (it.x >= room0.x && it.x <= room1.x && it.y >= room0.y && it.y <= room1.y)
                } ?: true
            } ?: true
        }
        return super.pickAction(npc)
    }
}

@Serializable
class IdleWander(
    val wanderChance: Float
) : Idle() {
    override fun toString() = "IdleWander($wanderChance)"
    override fun pickAction(npc: NPC): Action {
        if (Dice.chance(wanderChance)) {
            return wander(npc) { true }
        }
        return super.pickAction(npc)
    }
}
