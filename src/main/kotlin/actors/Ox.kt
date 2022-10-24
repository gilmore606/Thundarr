package actors

import kotlinx.serialization.Serializable
import render.tilesets.Glyph

@Serializable
class Ox : NPC() {
    override fun glyph() = Glyph.CATTLE

    override fun name() = "ox"

    override fun description() = "A big lazy ruminant covered in short wiry bluish fur."

}
