package things

import kotlinx.serialization.Serializable
import render.tilesets.Glyph


@Serializable
sealed class Food : Portable() { }

@Serializable
class Apple : Food() {
    override fun glyph() = Glyph.FRUIT
    override fun name() = "apple"
    override val kind = Kind.APPLE
}

@Serializable
class EnergyDrink : Food() {
    override fun glyph() = Glyph.BOTTLE
    override fun name() = "Monster energy drink"
    override val kind = Kind.ENERGY_DRINK
}
