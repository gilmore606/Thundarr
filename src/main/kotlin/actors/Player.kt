package actors

import App
import actors.actions.Action
import util.Glyph
import util.log

class Player : Actor(
    Glyph.PLAYER,
    1.0f
) {

    override fun queue(action: Action) {
        super.queue(action)
        App.level.director.runQueue()
    }

    override fun defaultAction(): Action? = null

    override fun moveTo(x: Int, y: Int) {
        super.moveTo(x, y)
        App.level.setPov(x, y)
    }
}
