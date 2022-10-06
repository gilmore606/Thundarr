package render.tileholders

import world.Level
import render.tilesets.TileSet

class SimpleTile(
    set: TileSet,
    val tx: Int,
    val ty: Int
): TileHolder(set) {

    override fun getTextureIndex(level: Level?, x: Int, y: Int): Int {
        return ty * set.tilesPerRow + tx
    }

}
