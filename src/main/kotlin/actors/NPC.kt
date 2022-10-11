package actors

import actors.actions.Action
import actors.actions.Wait
import kotlinx.serialization.Serializable
import util.Glyph

sealed class NPC(
    override val glyph: Glyph,
    override val speed: Float
) : Actor(glyph, speed) {

    final override fun defaultAction(): Action {
        return super.nextAction() ?: pickAction()
    }

    // NPC AI returns actions here.
    open fun pickAction(): Action = Wait(1f)

}
