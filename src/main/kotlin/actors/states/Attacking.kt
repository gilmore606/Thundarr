package actors.states

import actors.NPC
import actors.actions.Action
import actors.actions.Attack
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.XY
import util.distanceBetween
import world.path.Pather

@Serializable
class Attacking(
    val targetID: String,
    val origin: XY? = null,
    val maxChaseRange: Int = 0,
) : State() {

    override fun toString() = "Attacking (target $targetID)"

    override fun enter(npc: NPC) {
        App.level.director.getActor(targetID)?.also { target ->
            Pather.subscribe(npc, target, npc.visualRange().toInt())
        }
    }

    override fun leave(npc: NPC) {
        App.level.director.getActor(targetID)?.also { target ->
            Pather.unsubscribe(npc, target)
        }
    }

    override fun considerState(npc: NPC) {
        npc.apply {
            if (!canSee(getActor(targetID))) {
                popState()
            }
            origin?.also { origin ->
                if (distanceBetween(origin, npc.xy) > maxChaseRange) {
                    popState()
                }
            }
        }
    }

    override fun pickAction(npc: NPC): Action {
        npc.apply {
            getActor(targetID)?.also { target ->
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
        if (targetID == App.player.id) {
            drawIt(Glyph.HOSTILE_ICON)
        } else {
            drawIt(Glyph.HOSTILE_OTHER_ICON)
        }
    }
}
