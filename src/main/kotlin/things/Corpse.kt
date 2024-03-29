package things

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.aOrAn
import util.iterateAndEmpty

@Serializable
sealed class GenericCorpse(
    val departedName: String? = null
) : Container(), Temporal {

    var rot = 0f
    private fun rotTime() = 500f

    override fun temporalDone() = holder == null

    override fun name() = departedName?.let { "$it corpse" } ?: "corpse"
    override fun description() = "The mangled, lifeless organic remains of " + (departedName?.aOrAn() ?: "some unfortunate creature") + "."
    override fun glyph() = Glyph.CORPSE
    override fun isPortable(): Boolean = false
    override fun openVerb() = "search"
    override fun isEmptyMsg() = "You find nothing useful."

    override fun advanceTime(delta: Float) {
        rot += delta
        if (rot > rotTime()) {
            rot()
        }
    }

    private fun rot() {
        onRot()
        level()?.also { level ->
            xy().also { xy ->
                contents().iterateAndEmpty { it.moveTo(level, xy.x, xy.y) }
            }
        } ?: run {
            contents().iterateAndEmpty { it.moveTo(null) }
        }
        moveTo(null)
    }

    private fun onRot() { }
}

@Serializable
class Corpse(
    val corpseName: String? = null
) : GenericCorpse(corpseName) {
    override val tag = Tag.CORPSE
    override fun glyph() = Glyph.CORPSE
}

@Serializable
class BugCorpse(
    val corpseName: String? = null
) : GenericCorpse(corpseName) {
    override val tag = Tag.BUG_CORPSE
    override fun glyph() = Glyph.GOO_CORPSE
}
