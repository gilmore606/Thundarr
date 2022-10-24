package actors

import App
import actors.actions.Action
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import world.Entity
import world.Level

@Serializable
class Player : Actor() {
    override fun glyph() = Glyph.PLAYER
    override fun name() = "Thundarr"
    override fun gender() = Entity.Gender.MALE
    override fun description() = "A stalwart blond barbarian in a leather tunic."

    override fun defaultAction(): Action? = null

    override fun moveTo( level: Level, x: Int, y: Int, fromLevel: Level?) {
        super.moveTo(level, x, y, fromLevel)
        App.level.setPov(x, y)
    }
}
