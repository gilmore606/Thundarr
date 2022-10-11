package render.tilesets

import render.tileholders.SimpleTile
import util.Glyph

fun UITileSet() =
    TileSet("tiles_ui.png", 3, 2).apply {
        setTile(Glyph.CURSOR, SimpleTile(this, 0, 0))
        setTile(Glyph.BOX_BG, SimpleTile(this, 1, 0))
        setTile(Glyph.BOX_SHADOW, SimpleTile(this, 0, 1))
        setTile(Glyph.BOX_BORDER, SimpleTile(this, 1, 1))
        setTile(Glyph.LOGO_MOON, SimpleTile(this, 2, 0))
        setTile(Glyph.LOGO_OOKLA, SimpleTile(this, 2, 1))
    }
