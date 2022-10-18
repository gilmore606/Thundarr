package render.tileholders

import render.tilesets.Glyph
import render.tilesets.TileSet

class OverlapTile(
    set: TileSet
) : VariantsTile(set) {

    private val overlapTypes = mutableSetOf<Glyph>()

    fun addOverlap(type: Glyph) {
        overlapTypes.add(type)
    }

}
