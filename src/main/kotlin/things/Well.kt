package things

import actors.actors.Actor
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.panels.Console

@Serializable
class Well : Thing() {

    override val tag = Tag.WELL
    override fun name() = "well"
    override fun description() = "A stone well with a crossbeam and bucket.  Make a wish."
    override fun glyph() = Glyph.WELL
    override fun isPortable() = false
    override fun isOpaque() = false
    override fun isBlocking(actor: Actor) = true

    override fun uses() = mutableMapOf(
        UseTag.CONSUME to Use("drink from " + name(), 2.0f,
            canDo = { actor,x,y,targ -> isNextTo(actor) },
            toDo = { actor,level,x,y -> doDrink(actor) }
            )
    )

    private fun doDrink(actor: Actor) {
        Console.sayAct("Ahh, refreshing.", "%DN drinks from %dd.", actor, this)
    }
}
