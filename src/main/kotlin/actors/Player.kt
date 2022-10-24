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

    override fun canAct() = queuedActions.isNotEmpty()
    override fun isActing() = true
    override fun defaultAction(): Action? = null

}
