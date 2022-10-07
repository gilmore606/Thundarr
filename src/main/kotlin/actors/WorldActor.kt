package actors

import actors.actions.Action
import actors.actions.WorldAction
import util.Glyph

object WorldActor : Actor( Glyph.CURSOR, 1f) {
    override fun defaultAction(): Action = WorldAction()
}
