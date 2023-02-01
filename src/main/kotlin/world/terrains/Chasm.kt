package world.terrains

import render.tilesets.Glyph

object Chasm : Terrain(Type.TERRAIN_CHASM, Glyph.CHASM, false, true, false, false) {

    override fun name() = "chasm"

}
