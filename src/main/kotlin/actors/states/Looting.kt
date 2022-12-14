package actors.states

import actors.NPC
import actors.actions.Action
import actors.actions.Get
import things.Thing
import util.XY
import util.hasOneWhere
import util.log
import world.level.Level
import world.path.Pather

// Trying to get a known seen item.
class Looting(
    val targetXY: XY,
    val thingTag: String
) : State() {

    override fun enter(npc: NPC) {
        Pather.subscribe(npc, npc, npc.visualRange())
    }

    override fun leave(npc: NPC) {
        Pather.unsubscribe(npc, npc)
    }

    override fun considerState(npc: NPC) {
        npc.apply {
            if (!stillThere(level)) {
                popState()
            }
        }
    }

    override fun pickAction(npc: NPC): Action {
        npc.apply {
            if (xy() == targetXY) {
                log.info("found loot")
                popState()
                theThing(level)?.also { return Get(it) }
            } else {
                stepToward(targetXY)?.also { return it }
            }
            //popState()
        }
        return super.pickAction(npc)
    }

    private fun stillThere(level: Level?): Boolean =
        level?.thingsAt(targetXY.x, targetXY.y)?.hasOneWhere { it.thingTag() == thingTag } ?: false

    private fun theThing(level: Level?): Thing? =
        level?.thingsAt(targetXY.x, targetXY.y)?.firstOrNull { it.thingTag() == thingTag }

}
