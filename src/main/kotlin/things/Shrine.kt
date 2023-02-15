package things

import actors.Actor
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.panels.Console

@Serializable
class Shrine : Thing() {

    override fun name() = "shrine"
    override fun description() = "A stone shrine erected to honor the Lords of Light."
    override fun glyph() = Glyph.SHRINE
    override fun isPortable() = false
    override fun isOpaque() = false
    override fun isBlocking() = true

    override fun uses() = mapOf(
        UseTag.CONSUME to Use("pray to " + name(), 2.0f,
            canDo = { actor,x,y,targ -> isNextTo(actor) },
            toDo = { actor,level,x,y -> doPray(actor) }
        )
    )

    private fun doPray(actor: Actor) {
        Console.sayAct("You whisper a prayer for protection to the Lords of Light.",  "%DN kneels before %dd and whispers a prayer.", actor, this)
    }

}
