package actors

import actors.actions.Action
import actors.actions.Wait
import util.Glyph

class NPC(
    glyph: Glyph,
    speed: Float
) : Actor(glyph, speed) {

    override fun nextAction(): Action {
        return super.nextAction() ?: Wait(1f)
    }

}
