package actors

import App
import actors.actions.Action
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import world.Level

@Serializable
class Player : Actor(
    Glyph.PLAYER,
    1.0f
) {
    override fun defaultAction(): Action? = null

    override fun moveTo( level: Level, x: Int, y: Int, fromLevel: Level?) {
        super.moveTo(level, x, y, fromLevel)
        App.level.setPov(x, y)
    }
}
