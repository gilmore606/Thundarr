package actors

import actors.actions.Action
import actors.actions.Wait
import kotlinx.serialization.Serializable
import render.tilesets.Glyph

@Serializable
sealed class NPC : Actor() {

    final override fun defaultAction(): Action = pickAction()

    // NPC AI returns actions here.
    open fun pickAction(): Action = Wait(1f)

}
