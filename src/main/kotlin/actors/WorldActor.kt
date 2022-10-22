package actors

import actors.actions.Action
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import world.Level

@Serializable
object WorldActor : Actor( Glyph.CURSOR, 1f) {
    init { real = false }

    override fun defaultAction(): Action = WorldAction
}


object WorldAction : Action(1f) {
    override fun execute(actor: Actor, level: Level) {
        App.advanceTime(1f * level.timeScale())
    }
}
