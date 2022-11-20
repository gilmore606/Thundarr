package actors.states

import actors.NPC
import actors.actions.Action
import actors.actions.Move
import actors.actions.Wait
import audio.Speaker
import kotlinx.serialization.Serializable
import ui.panels.Console
import util.DIRECTIONS
import util.Dice
import util.XY

@Serializable
sealed class Idle : State() {

    override fun considerState(npc: NPC) {
        val canSeePlayer = npc.canSee(App.player)
        if (canSeePlayer && !npc.metPlayer) {
            npc.meetPlayerMsg()?.also {
                Console.say(it)
                npc.talkSound(App.player)?.also { Speaker.world(it, source = npc.xy())}
            }
            npc.metPlayer = true
        }

        if (npc.isHostile() && canSeePlayer) {
            npc.changeState(npc.hostileResponseState(App.player.id))
        }
    }

    protected fun wander(npc: NPC, isOK: ((XY)->Boolean) = { true }): Action {
        npc.apply { level?.also { level ->
            placeMemory["lastWander"] ?: run { placeMemory["lastWander"] = XY(0,0) }
            placeMemory["lastWander2"] ?: run { placeMemory["lastWander2"] = XY(0,0) }
            val dirs = DIRECTIONS.toMutableList()
            while (dirs.isNotEmpty()) {
                val dir = dirs.removeAt(Dice.zeroTil(dirs.size))
                val dest = xy + dir
                if (level.isWalkableFrom(xy, dir) && isOK(dest)) {
                    if (placeMemory["lastWander"] != dest && placeMemory["lastWander2"] != dest) {
                        placeMemory["lastWander2"]!!.x = placeMemory["lastWander"]!!.x
                        placeMemory["lastWander2"]!!.y = placeMemory["lastWander"]!!.y
                        placeMemory["lastWander"]!!.x = dest.x
                        placeMemory["lastWander"]!!.y = dest.y
                        return Move(dir)
                    }
                }
            }
            placeMemory["lastWander"]!!.x = 0
            placeMemory["lastWander"]!!.y = 0
            placeMemory["lastWander2"]!!.x = 0
            placeMemory["lastWander2"]!!.y = 0
        }}
        return Wait(1f)
    }

}

@Serializable
class IdleDoNothing : Idle()

@Serializable
class IdleInRoom : Idle() {

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
