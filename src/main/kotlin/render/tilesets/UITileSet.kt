package render.tilesets

import render.tileholders.SimpleTile
import util.Glyph

fun UITileSet() =
    TileSet("tiles_ui.png", 2, 2).apply {
        setTile(Glyph.CURSOR, SimpleTile(this, 0, 0))
        setTile(Glyph.BOX_BG, SimpleTile(this, 1, 0))
        setTile(Glyph.BOX_SHADOW, SimpleTile(this, 0, 1))
        setTile(Glyph.BOX_BORDER, SimpleTile(this, 1, 1))
    }
