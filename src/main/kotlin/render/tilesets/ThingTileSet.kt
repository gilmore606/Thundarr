package render.tilesets

import render.tileholders.SimpleTile

fun ThingTileSet() =
    TileSet("tiles_thing.png", 2, 1).apply {

        setTile(Glyph.TREE, SimpleTile(this, 0, 0))
        setTile(Glyph.LIGHTBULB, SimpleTile(this, 1, 0))
    }
