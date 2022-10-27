package actors

import actors.actions.Action
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice

@Serializable
class Herder : NPC() {

    override fun glyph() = Glyph.HERDER
    override fun name() = "herdsman"
    override fun description() = "A peasant farm worker.  He looks oppressed, possibly by wizards."
    override fun converseLines() = listOf(
        "Shoveling ox poo all day for a wizard.  It's a living.",
        "The love of an ox, is not like that of a square.  You know what I mean?",
        "You sure are a big guy!",
        "Shouldn't you be doing your job for the wizard?"
    )

    override fun pickAction(): Action {
        if (awareness == Awareness.AWARE && Dice.chance(0.5f)) {
            wander()?.also { return it }
        }
        return super.pickAction()
    }

}
