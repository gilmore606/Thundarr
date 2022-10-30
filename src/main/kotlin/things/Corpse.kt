package things

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.iterateAndEmpty

@Serializable
class Corpse : Container(), Temporal {

    var rot = 0f
    open fun rotTime() = 20f

    override fun temporalDone() = holder == null

    override fun name() = "corpse"
    override fun description() = "The mangled body of some unfortunate creature.  It's so damaged you can hardly tell what it was."
    override fun glyph() = Glyph.CORPSE
    override fun isPortable(): Boolean = false
    override fun openVerb() = "search"

    override fun advanceTime(delta: Float) {
        rot += delta
        if (rot > rotTime()) {
            rot()
        }
    }

    fun rot() {
        onRot()
        level()?.also { level ->
            xy()?.also { xy ->
                contents().iterateAndEmpty { it.moveTo(level, xy.x, xy.y) }
            }
        } ?: run {
            contents().iterateAndEmpty { it.moveTo(null) }
        }
        moveTo(null)
    }

    open fun onRot() { }
}
