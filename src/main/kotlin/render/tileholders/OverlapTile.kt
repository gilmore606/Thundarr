package render.tileholders

import render.tilesets.Glyph
import render.tilesets.TileSet
import util.XY
import world.Level
import world.terrains.Terrain

class OverlapTile(
    set: TileSet
) : VariantsTile(set) {

    private val overlapTypes = mutableSetOf<Glyph>()

    fun addOverlap(type: Glyph) {
        overlapTypes.add(type)
    }

    fun overlapsIn(level: Level, x: Int, y: Int, dir: XY): Boolean =
        Terrain.get(level.getTerrain(x - dir.x, y - dir.y)).glyph() in overlapTypes

}
