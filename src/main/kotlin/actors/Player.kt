package actors

import App
import actors.actions.Action
import kotlinx.serialization.Serializable
import util.Glyph

@Serializable
class Player : Actor(
    Glyph.PLAYER,
    1.0f
) {
    override fun defaultAction(): Action? = null

    override fun moveTo(x: Int, y: Int) {
        super.moveTo(x, y)
        App.level.setPov(x, y)
    }
}
