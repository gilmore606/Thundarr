package render.tilesets

import render.tileholders.SimpleTile

fun MobTileSet() =
    TileSet("tiles_mob.png", 7, 4).apply {
        setTile(Glyph.PLAYER, SimpleTile(this, 0, 0))
        setTile(Glyph.SKELETON, SimpleTile(this, 0, 2))
        setTile(Glyph.SKULL, SimpleTile(this, 1, 3))
    }
