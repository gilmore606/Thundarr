package world.stains

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice

@Serializable
class Scorch : Stain() {

    override fun glyph() = Glyph.SCORCHMARK
    override fun name() = "scorch"
    override fun lifespan() = 1000.0
    override fun stackType() = Type.SCORCH

    var spawned = false

    init {
        if (!spawned) {
            spawned = true
            sizeMod = Dice.float(-0.2f, 0.1f)
            posModX = Dice.float(-0.1f, 0.1f)
            posModY = Dice.float(-0.1f, 0.1f)
            alphaMod = Dice.float(-0.1f, 0.1f)
        }
    }
}
