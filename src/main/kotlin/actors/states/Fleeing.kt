package actors.states

import actors.actors.NPC
import actors.actions.Action
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import path.Pather

@Serializable
class Fleeing(
    val targetID: String
    ) : State() {

    override fun toString() = "Fleeing (target $targetID)"

    override fun commentLine() = "I've got to get out of here!"

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
            popState()
        }
        return super.pickAction(npc)
    }

    override fun drawStatusGlyph(drawIt: (Glyph) -> Unit): Boolean {
        drawIt(Glyph.FLEEING_ICON)
        return true
    }

    override fun idleBounceMs() = 500
    override fun idleBounce() = -0.05f
}
