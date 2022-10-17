package render.tilesets

import render.tileholders.SimpleTile
import render.tileholders.VariantsTile

fun ThingTileSet() =
    TileSet("tiles_thing.png", 2, 2).apply {

        setTile(Glyph.TREE, VariantsTile(this).apply {
            add(0.4f, 0, 1)
            add(0.3f, 0, 0)
            add(0.3f, 1, 1)
        })
        setTile(Glyph.LIGHTBULB, SimpleTile(this, 1, 0))
    }
