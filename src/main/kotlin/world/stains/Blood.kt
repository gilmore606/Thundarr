package world.stains

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice

@Serializable
class Blood : Stain() {

    override fun glyph() = Glyph.BLOODSTAIN
    override fun name() = "blood"
    override fun lifespan() = 100.0
    override fun stackType() = Type.BLOOD

    var spawned = false

    init {
        if (!spawned) {
            spawned = true
            sizeMod = Dice.float(-0.25f, 0.2f)
            posModX = Dice.float(-0.3f, 0.3f)
            posModY = Dice.float(-0.3f, 0.3f)
            alphaMod = Dice.float(-0.3f, 0.3f)
        }
    }
}
