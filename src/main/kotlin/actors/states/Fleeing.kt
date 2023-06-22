package actors.states

import actors.Actor
import actors.NPC
import actors.actions.Action
import actors.actions.Say
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import world.path.Pather

@Serializable
class Fleeing(
    val targetID: String
    ) : State() {

    override fun toString() = "Fleeing (target $targetID)"

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
                return
            }
        }
    }

    override fun pickAction(npc: NPC): Action {
        npc.apply {
            getActor(targetID)?.also {
                stepAwayFrom(it)?.also { return it }
            }
        }
        return super.pickAction(npc)
    }

    override fun drawStatusGlyphs(drawIt: (Glyph) -> Unit) {
        drawIt(Glyph.FLEEING_ICON)
    }

}
