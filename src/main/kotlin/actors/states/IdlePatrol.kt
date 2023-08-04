package actors.states

import actors.actors.NPC
import actors.actions.Action
import actors.actions.Move
import actors.actions.Wait
import actors.actors.Actor
import actors.actors.Citizen
import kotlinx.serialization.Serializable
import util.*

@Serializable
class IdlePatrol(
    val wanderChance: Float,
    val bounds: Rect,
    val stayOutdoors: Boolean = false
) : Idle() {

    var heading: XY = CARDINALS.random()

    override fun toString() = "IdlePatrol($bounds)"

    override fun pickAction(npc: NPC): Action {
        if (!Dice.chance(wanderChance)) return Wait(1f)

        npc.apply { level?.also { level ->
            val dirs = mutableListOf<XY>()
            val testdirs = if (Dice.flip()) listOf(heading) else heading.dirPlusDiagonals()
            testdirs.forEach { dir ->
                if (level.isWalkableFrom(npc, xy, dir)) {
                    if (bounds.contains(xy + dir)) {
                        if (!stayOutdoors || !level.isRoofedAt(xy.x + dir.x, xy.y + dir.y)) {
                            dirs.add(dir)
                        }
                    } else if (Dice.chance(0.3f)) {
                        heading = CARDINALS.random()
                    }
                }
            }
            if (dirs.isNotEmpty()) {
                val dir = dirs.random()
                return Move(dir)
            } else {
                heading = CARDINALS.random()
            }
        }}

        return Wait(1f)
    }

    override fun considerState(npc: NPC) {
        npc.apply {
            if (!bounds.contains(xy)) {
                pushState(ReturnToArea(bounds))
                return
            }

            entitiesSeen { it is NPC && it !is Citizen && it.state is Attacking }.keys.firstOrNull()?.also { aggressor ->
                pushState(Attacking((aggressor as NPC).id))
            }
        }
        super.considerState(npc)
    }
}
