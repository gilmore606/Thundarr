package render.tilesets

import render.tileholders.SimpleTile

fun ThingTileSet() =
    TileSet("tiles_thing.png", 1, 1).apply {

        setTile(Glyph.TREE, SimpleTile(this, 0, 0))

    }
