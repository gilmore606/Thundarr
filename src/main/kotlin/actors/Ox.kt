package actors

import actors.NPC
import render.tilesets.Glyph

class Ox : NPC() {
    override fun glyph() = Glyph.CATTLE

    override fun name() = "ox"

    override fun description() = "A big lazy ruminant covered in short wiry bluish fur."

}
