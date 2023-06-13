package actors.states

import actors.NPC
import actors.actions.Action
import actors.actions.Move
import actors.actions.Wait
import util.*

class IdlePatrol(
    val wanderChance: Float,
    val bounds: Rect,
) : Idle() {

    var heading: XY = CARDINALS.random()

    override fun pickAction(npc: NPC): Action {
        if (!Dice.chance(wanderChance)) return Wait(1f)

        npc.apply { level?.also { level ->
            val dirs = mutableListOf<XY>()
            val testdirs = if (Dice.flip()) listOf(heading) else heading.dirPlusDiagonals()
            testdirs.forEach { dir ->
                if (level.isWalkableFrom(npc, xy, dir)) {
                    if (bounds.contains(xy + dir)) {
                        dirs.add(dir)
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
}
