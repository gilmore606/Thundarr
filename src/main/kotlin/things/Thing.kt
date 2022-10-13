package things

import kotlinx.serialization.Serializable
import render.tilesets.Glyph

@Serializable
class Thing(
    val glyph: Glyph,
    val isOpaque: Boolean = false,
    val isBlocking: Boolean = false
) {

    fun glyph() = glyph

}
