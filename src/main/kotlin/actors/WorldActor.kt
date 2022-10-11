package actors

import actors.actions.Action
import actors.actions.WorldAction
import kotlinx.serialization.Serializable
import util.Glyph

@Serializable
class WorldActor : Actor( Glyph.CURSOR, 1f) {
    override fun defaultAction(): Action = WorldAction()
}
