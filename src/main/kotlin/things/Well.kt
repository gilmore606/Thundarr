package things

import actors.Actor
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.panels.Console

@Serializable
class Well : Thing() {

    override val tag = Tag.THING_WELL
    override fun name() = "well"
    override fun description() = "A stone well with a crossbeam and bucket.  Make a wish."
    override fun glyph() = Glyph.WELL
    override fun isPortable() = false
    override fun isOpaque() = false
    override fun isBlocking() = true

    override fun uses() = mapOf(
        UseTag.CONSUME to Use("drink from " + name(), 2.0f,
            canDo = { actor,x,y,targ -> isNextTo(actor) },
            toDo = { actor,level,x,y -> doDrink(actor) }
            )
    )

    private fun doDrink(actor: Actor) {
        Console.sayAct("Ahh, refreshing.", "%DN drinks from %dd.", actor, this)
    }
}
