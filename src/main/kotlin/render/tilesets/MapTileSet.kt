package render.tilesets

import render.tileholders.SimpleTile

fun MapTileSet() =
    TileSet(SpriteSheets.Sheet.MapSprites).apply {

        setTile(Glyph.MAP_FOREST, SimpleTile(this, 0, 0))
        setTile(Glyph.MAP_PLAIN, SimpleTile(this, 1, 0))
        setTile(Glyph.MAP_WATER, SimpleTile(this, 2, 0))
        setTile(Glyph.MAP_SWAMP, SimpleTile(this, 3, 0))
        setTile(Glyph.MAP_DESERT, SimpleTile(this, 0, 1))

        setTile(Glyph.MAP_RIVER_NSE, SimpleTile(this, 1, 1))
        setTile(Glyph.MAP_RIVER_NSW, SimpleTile(this, 2, 1))
        setTile(Glyph.MAP_RIVER_NWE, SimpleTile(this, 3, 1))
        setTile(Glyph.MAP_RIVER_NS, SimpleTile(this, 0, 2))
        setTile(Glyph.MAP_RIVER_WE, SimpleTile(this, 1, 2))
        setTile(Glyph.MAP_RIVER_WES, SimpleTile(this, 2, 2))
        setTile(Glyph.MAP_RIVER_WN, SimpleTile(this, 3, 2))
        setTile(Glyph.MAP_RIVER_NE, SimpleTile(this, 0, 3))
        setTile(Glyph.MAP_RIVER_WS, SimpleTile(this, 1, 3))
        setTile(Glyph.MAP_RIVER_SE, SimpleTile(this, 2, 3))

        setTile(Glyph.MAP_PLAYER, SimpleTile(this, 3, 3))
    }
