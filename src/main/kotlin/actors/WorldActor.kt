package actors

import actors.actions.Action
import actors.actions.WorldAction
import kotlinx.serialization.Serializable
import render.tilesets.Glyph

@Serializable
class WorldActor : Actor( Glyph.CURSOR, 1f) {
    init {
        real = false
    }

    override fun defaultAction(): Action = WorldAction()
}
