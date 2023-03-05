package render.tilesets

import render.tileholders.SimpleTile

fun MapTileSet() =
    TileSet(SpriteSheets.Sheet.MapSprites).apply {

        setTile(Glyph.MAP_FOREST, SimpleTile(this, 0, 0))
        setTile(Glyph.MAP_PLAIN, SimpleTile(this, 1, 0))
        setTile(Glyph.MAP_WATER, SimpleTile(this, 2, 0))
        setTile(Glyph.MAP_GLACIER, SimpleTile(this, 4, 2))
        setTile(Glyph.MAP_SWAMP, SimpleTile(this, 3, 0))
        setTile(Glyph.MAP_DESERT, SimpleTile(this, 0, 1))
        setTile(Glyph.MAP_MOUNTAIN, SimpleTile(this, 4, 0))
        setTile(Glyph.MAP_RUINS, SimpleTile(this, 5, 0))
        setTile(Glyph.MAP_HILL, SimpleTile(this, 5, 1))
        setTile(Glyph.MAP_FORESTHILL, SimpleTile(this, 5, 4))
        setTile(Glyph.MAP_SCRUB, SimpleTile(this, 5, 2))
        setTile(Glyph.MAP_SUBURB, SimpleTile(this, 5, 3))

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

        setTile(Glyph.MAP_ROAD_NSE, SimpleTile(this, 4, 4))
        setTile(Glyph.MAP_ROAD_NSW, SimpleTile(this, 4, 5))
        setTile(Glyph.MAP_ROAD_NWE, SimpleTile(this, 0, 4))
        setTile(Glyph.MAP_ROAD_NS, SimpleTile(this, 3, 5))
        setTile(Glyph.MAP_ROAD_WE, SimpleTile(this, 0, 5))
        setTile(Glyph.MAP_ROAD_WES, SimpleTile(this, 2, 5))
        setTile(Glyph.MAP_ROAD_WN, SimpleTile(this, 2, 4))
        setTile(Glyph.MAP_ROAD_NE, SimpleTile(this, 1, 4))
        setTile(Glyph.MAP_ROAD_WS, SimpleTile(this, 1, 5))
        setTile(Glyph.MAP_ROAD_SE, SimpleTile(this, 3, 4))
        setTile(Glyph.MAP_ROAD_NSEW, SimpleTile(this, 4, 3))

        setTile(Glyph.MAP_TRAIL_NSE, SimpleTile(this, 6, 6))
        setTile(Glyph.MAP_TRAIL_NSW, SimpleTile(this, 6, 7))
        setTile(Glyph.MAP_TRAIL_NWE, SimpleTile(this, 2, 6))
        setTile(Glyph.MAP_TRAIL_NS, SimpleTile(this, 5, 7))
        setTile(Glyph.MAP_TRAIL_WE, SimpleTile(this, 2, 7))
        setTile(Glyph.MAP_TRAIL_WES, SimpleTile(this, 4, 7))
        setTile(Glyph.MAP_TRAIL_WN, SimpleTile(this, 4, 6))
        setTile(Glyph.MAP_TRAIL_NE, SimpleTile(this, 3, 6))
        setTile(Glyph.MAP_TRAIL_WS, SimpleTile(this, 3, 7))
        setTile(Glyph.MAP_TRAIL_SE, SimpleTile(this, 5, 6))
        setTile(Glyph.MAP_TRAIL_NSEW, SimpleTile(this, 1, 7))

        setTile(Glyph.MAP_HABITAT_HOT_A, SimpleTile(this, 6, 0))
        setTile(Glyph.MAP_HABITAT_HOT_B, SimpleTile(this, 6, 1))
        setTile(Glyph.MAP_HABITAT_TEMP_A, SimpleTile(this, 6, 2))
        setTile(Glyph.MAP_HABITAT_TEMP_B, SimpleTile(this, 6, 3))
        setTile(Glyph.MAP_HABITAT_COLD_A, SimpleTile(this, 6, 4))
        setTile(Glyph.MAP_HABITAT_COLD_B, SimpleTile(this, 6, 5))
        setTile(Glyph.MAP_HABITAT_ARCTIC, SimpleTile(this, 5, 5))

        setTile(Glyph.MAP_PLAYER, SimpleTile(this, 3, 3))
        setTile(Glyph.MAP_CITY, SimpleTile(this, 4, 1))
        setTile(Glyph.MAP_VILLAGE, SimpleTile(this, 0, 6))
        setTile(Glyph.MAP_CAVE, SimpleTile(this, 0, 7))
    }
