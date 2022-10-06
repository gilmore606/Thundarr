package render.tilesets

import render.tileholders.SimpleTile
import util.Tile

fun UITileSet() =
    TileSet("tiles_ui.png", 1, 1).apply {
        setTile(Tile.CURSOR, SimpleTile(this, 0, 0))
    }
