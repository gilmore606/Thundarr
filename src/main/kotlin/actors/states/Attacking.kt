package actors.states

import actors.NPC
import actors.actions.Action
import actors.actions.Attack
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.XY
import world.path.Pather

@Serializable
class Attacking(
    val targetId: String
) : State() {

    override fun toString() = "Attacking (target $targetId)"

    override fun enter(npc: NPC) {
        App.level.director.getActor(targetId)?.also { target ->
            Pather.subscribe(npc, target, npc.visualRange())
        }
    }

    override fun leave(npc: NPC) {
        App.level.director.getActor(targetId)?.also { target ->
            Pather.unsubscribe(npc, target)
        }
    }

    override fun considerState(npc: NPC) {
        npc.apply {
            if (!canSee(getActor(targetId))) {
                changeState(hostileLossState(targetId))
            }
        }
    }

    override fun pickAction(npc: NPC): Action {
        npc.apply {
            getActor(targetId)?.also { target ->
                if (entitiesNextToUs().contains(target)) {
                    return Attack(target, XY(target.xy.x - npc.xy.x, target.xy.y - npc.xy.y))
                } else {
                    stepToward(target)?.also { return it }
                }
            }
        }
        return super.pickAction(npc)
    }

    override fun drawStatusGlyphs(drawIt: (Glyph) -> Unit) {
        if (targetId == App.player.id) {
            drawIt(Glyph.HOSTILE_ICON)
        } else {
            drawIt(Glyph.HOSTILE_OTHER_ICON)
        }
    }
}
