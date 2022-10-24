package actors

import actors.actions.Action
import actors.actions.Wait
import render.tilesets.Glyph

sealed class NPC : Actor() {

    final override fun defaultAction(): Action {
        return super.nextAction() ?: pickAction()
    }

    // NPC AI returns actions here.
    open fun pickAction(): Action = Wait(1f)

}
