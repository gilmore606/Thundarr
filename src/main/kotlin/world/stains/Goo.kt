package world.stains

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice

@Serializable
class Goo : Stain() {

    override fun glyph() = Glyph.BLOODSTAIN
    override fun name() = "goo"
    override fun lifespan() = 500.0
    override fun stackType() = Type.GOO
    override fun hue() = 0.8f

    var spawned = false

    init {
        if (!spawned) {
            spawned = true
            sizeMod = Dice.float(-0.35f, 0.1f)
            posModX = Dice.float(-0.3f, 0.3f)
            posModY = Dice.float(-0.3f, 0.3f)
            alphaMod = Dice.float(-0.3f, 0.3f)
        }
    }
}
