package actors.states

import kotlinx.serialization.Serializable
import render.tilesets.Glyph

@Serializable
class Fleeing(
    val targetId: String
    ) : State() {

    override fun drawStatusGlyphs(drawIt: (Glyph) -> Unit) {
        drawIt(Glyph.FLEEING_ICON)
    }

}
