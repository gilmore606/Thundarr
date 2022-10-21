package things

import kotlinx.serialization.Serializable
import render.tilesets.Glyph


@Serializable
sealed class Food : Portable() { }

@Serializable
class Apple : Food() {
    override fun glyph() = Glyph.FRUIT
    override fun name() = "apple"
}
