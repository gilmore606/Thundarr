package world.stains

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice

@Serializable
class Blood : Stain() {

    override fun glyph() = Glyph.BLOODSTAIN
    override fun name() = "blood"
    override fun lifespan() = 50.0
    override fun stackType() = Type.BLOOD

    init {
        sizeMod = Dice.float(-0.25f, 0.2f)
    }
}
