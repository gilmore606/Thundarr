package render.tilesets

import render.tileholders.SimpleTile
import util.Glyph

fun UITileSet() =
    TileSet("tiles_ui.png", 1, 1).apply {
        setTile(Glyph.CURSOR, SimpleTile(this, 0, 0))
    }
