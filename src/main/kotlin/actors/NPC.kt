package actors

import actors.actions.Action
import actors.actions.Wait
import util.Glyph

open class NPC(
    glyph: Glyph,
    speed: Float
) : Actor(glyph, speed) {

    final override fun defaultAction(): Action {
        return super.nextAction() ?: pickAction()
    }

    // NPC AI returns actions here.
    open fun pickAction(): Action = Wait(1f)

}
