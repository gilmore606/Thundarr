package render.tilesets

import render.tileholders.*

fun TerrainTileSet() =
    TileSet(SpriteSheets.Sheet.TerrainSprites).apply {

        setTile(Glyph.BLANK, SimpleTile(this, 2, 2))

        setTile(Glyph.OCCLUSION_SHADOWS_V, SimpleTile(this, 6, 2))
        setTile(Glyph.OCCLUSION_SHADOWS_H, SimpleTile(this, 7, 2))
        setTile(Glyph.SURF, AnimatedTile(this).apply {
            frameMs = 282
            add(11, 5)
            add(11, 6)
            add(11, 7)
            add(11, 6)
        })

        setTile(Glyph.CAVE_FLOOR, VariantsTile(this).apply {
            add(0.5f, 5, 4)
            add(0.5f, 5, 5)
        })

        setTile(Glyph.STONE_FLOOR, VariantsTile(this).apply {
            add(0.3f, 0, 0)
            add(0.3f, 0, 1)
            add(0.3f, 1, 0)
            add(0.1f, 1, 1)
        })

        setTile(Glyph.DIRT, VariantsTile(this).apply {
            add(0.5f, 0, 2)
            add(0.5f, 1, 2)
        })

        setTile(Glyph.ROCKS, VariantsTile(this).apply {
            add(0.5f, 7, 4)
            add(0.5f, 8, 4)
        })

        setTile(Glyph.GRASS, VariantsTile(this).apply {
            add(0.4f, 8, 0)
            add(0.4f, 8, 1)
            add(0.2f, 8, 2)
        })

        setTile(Glyph.SWAMP, VariantsTile(this).apply {
            add(0.8f, 6, 3)
            add(0.1f, 7, 3)
            add(0.1f, 8, 3)
        })

        setTile(Glyph.UNDERGROWTH, VariantsTile(this).apply {
            add(0.5f, 6, 5)
            add(0.5f, 7, 5)
        })

        setTile(Glyph.HARDPAN, SimpleTile(this, 8, 5))

        setTile(Glyph.BEACH, SimpleTile(this, 6, 4))

        setTile(Glyph.BRICK_WALL, WallTile(this, Glyph.BRICK_WALL).apply {
            add(WallTile.Slot.TOP, 0.33f, 2, 0)
            add(WallTile.Slot.TOP, 0.33f, 3, 0)
            add(WallTile.Slot.TOP, 0.34f, 4, 0)
            add(WallTile.Slot.LEFTTOP, 1f, 4, 0)
            add(WallTile.Slot.RIGHTTOP, 1f, 3, 0)
            add(WallTile.Slot.OUTSIDE_LEFTTOP, 1f, 5, 1)
            add(WallTile.Slot.OUTSIDE_RIGHTTOP, 1f, 6, 1)
            add(WallTile.Slot.LEFT, 0.5f, 5, 0)
            add(WallTile.Slot.LEFT, 0.5f, 5, 1)
            add(WallTile.Slot.RIGHT, 0.5f, 6, 0)
            add(WallTile.Slot.RIGHT, 0.5f, 6, 1)
            add(WallTile.Slot.LEFTBOTTOM, 1f, 7, 0)
            add(WallTile.Slot.OUTSIDE_LEFTBOTTOM, 1f, 3, 3)
            add(WallTile.Slot.BOTTOM, 0.33f, 2, 1)
            add(WallTile.Slot.BOTTOM, 0.33f, 3, 1)
            add(WallTile.Slot.BOTTOM, 0.34f, 4, 1)
            add(WallTile.Slot.RIGHTBOTTOM, 1f, 7, 1)
            add(WallTile.Slot.OUTSIDE_RIGHTBOTTOM, 1f, 2, 3)
            add(WallTile.Slot.CAP_BOTTOM, 1f, 2, 0)
            add(WallTile.Slot.CAP_TOP, 1f, 4, 2)
            add(WallTile.Slot.CAP_LEFT, 1f, 2, 0)
            add(WallTile.Slot.CAP_RIGHT, 1f, 3, 0)
            add(WallTile.Slot.FULL, 1f, 4, 2)
        })

        setTile(Glyph.CLIFF_WALL, WallTile(this, Glyph.CLIFF_WALL).apply {
            add(WallTile.Slot.TOP, 0.33f, 1, 4)
            add(WallTile.Slot.TOP, 0.33f, 2, 4)
            add(WallTile.Slot.TOP, 0.34f, 3, 4)
            add(WallTile.Slot.LEFTTOP, 1f, 1, 6)
            add(WallTile.Slot.RIGHTTOP, 1f, 2, 6)
            add(WallTile.Slot.OUTSIDE_LEFTTOP, 1f, 1, 5)
            add(WallTile.Slot.OUTSIDE_RIGHTTOP, 1f, 2, 5)
            add(WallTile.Slot.LEFT, 0.5f, 0, 5)
            add(WallTile.Slot.LEFT, 0.5f, 0, 6)
            add(WallTile.Slot.RIGHT, 0.5f, 4, 5)
            add(WallTile.Slot.RIGHT, 0.5f, 4, 6)
            add(WallTile.Slot.LEFTBOTTOM, 1f, 0, 7)
            add(WallTile.Slot.OUTSIDE_LEFTBOTTOM, 1f, 3, 6)
            add(WallTile.Slot.BOTTOM, 0.5f, 1, 7)
            add(WallTile.Slot.BOTTOM, 0.5f, 2, 7)
            add(WallTile.Slot.RIGHTBOTTOM, 1f, 4, 7)
            add(WallTile.Slot.OUTSIDE_RIGHTBOTTOM, 1f, 3, 7)
            add(WallTile.Slot.CAP_BOTTOM, 1f, 3, 5)
            add(WallTile.Slot.CAP_TOP, 1f, 5, 6)
            add(WallTile.Slot.CAP_LEFT, 1f, 5, 7)
            add(WallTile.Slot.CAP_RIGHT, 1f, 5, 7)
            add(WallTile.Slot.FULL, 1f, 5, 3)
        })

        setTile(Glyph.FOREST_WALL, WallTile(this, Glyph.FOREST_WALL).apply {
            add(WallTile.Slot.TOP, 1f, 10, 3)
            add(WallTile.Slot.LEFTTOP, 1f, 9, 3)
            add(WallTile.Slot.RIGHTTOP, 1f, 11, 3)
            add(WallTile.Slot.OUTSIDE_LEFTTOP, 1f, 9, 1)
            add(WallTile.Slot.OUTSIDE_RIGHTTOP, 1f, 11, 1)
            add(WallTile.Slot.LEFT, 1f, 11, 2)
            add(WallTile.Slot.RIGHT, 1f, 9, 2)
            add(WallTile.Slot.LEFTBOTTOM, 1f, 10, 2)
            add(WallTile.Slot.OUTSIDE_LEFTBOTTOM, 1f, 11, 1)
            add(WallTile.Slot.BOTTOM, 1f, 10, 1)
            add(WallTile.Slot.RIGHTBOTTOM, 1f, 10, 2)
            add(WallTile.Slot.OUTSIDE_RIGHTBOTTOM, 1f, 9, 1)
            add(WallTile.Slot.CAP_BOTTOM, 1f, 10, 3)
            add(WallTile.Slot.CAP_TOP, 1f, 10, 1)
            add(WallTile.Slot.CAP_LEFT, 1f, 9, 3)
            add(WallTile.Slot.CAP_RIGHT, 1f, 11, 3)
            add(WallTile.Slot.FULL, 1f, 10, 2)
        })

        setTile(Glyph.PINE_FOREST_WALL, WallTile(this, Glyph.PINE_FOREST_WALL).apply {
            add(WallTile.Slot.TOP, 1f, 7, 10)
            add(WallTile.Slot.LEFTTOP, 1f, 6, 10)
            add(WallTile.Slot.RIGHTTOP, 1f, 8, 10)
            add(WallTile.Slot.OUTSIDE_LEFTTOP, 1f, 6, 8)
            add(WallTile.Slot.OUTSIDE_RIGHTTOP, 1f, 8, 8)
            add(WallTile.Slot.LEFT, 1f, 8, 9)
            add(WallTile.Slot.RIGHT, 1f, 6, 9)
            add(WallTile.Slot.LEFTBOTTOM, 1f, 7, 9)
            add(WallTile.Slot.OUTSIDE_LEFTBOTTOM, 1f, 8, 8)
            add(WallTile.Slot.BOTTOM, 1f, 7, 8)
            add(WallTile.Slot.RIGHTBOTTOM, 1f, 7, 9)
            add(WallTile.Slot.OUTSIDE_RIGHTBOTTOM, 1f, 6, 8)
            add(WallTile.Slot.CAP_BOTTOM, 1f, 7, 10)
            add(WallTile.Slot.CAP_TOP, 1f, 7, 8)
            add(WallTile.Slot.CAP_LEFT, 1f, 6, 10)
            add(WallTile.Slot.CAP_RIGHT, 1f, 8, 10)
            add(WallTile.Slot.FULL, 1f, 7, 9)
        })

        setTile(Glyph.CHASM, ChasmTile(this, Glyph.CHASM).apply {
            add(ChasmTile.Slot.SOLO, 1f, 0, 9)
            add(ChasmTile.Slot.LEFTCAP, 1f, 0, 10)
            add(ChasmTile.Slot.RIGHTCAP, 1f, 1, 10)
            add(ChasmTile.Slot.TOPLEFT, 1f, 2, 8)
            add(ChasmTile.Slot.TOP, 1f, 3, 8)
            add(ChasmTile.Slot.TOPRIGHT, 1f, 4, 8)
            add(ChasmTile.Slot.TOPCAP, 1f, 5, 8)
            add(ChasmTile.Slot.LEFT, 1f, 2, 9)
            add(ChasmTile.Slot.MIDDLE, 1f, 3, 9)
            add(ChasmTile.Slot.RIGHT, 1f, 4, 9)
            add(ChasmTile.Slot.BOTTOMCAP, 1f, 5, 9)
            add(ChasmTile.Slot.BOTTOMLEFT, 1f, 2, 10)
            add(ChasmTile.Slot.BOTTOM, 1f, 3, 10)
            add(ChasmTile.Slot.BOTTOMRIGHT, 1f, 4, 10)
            add(ChasmTile.Slot.VERTICAL, 1f, 1, 9)
            add(ChasmTile.Slot.HORIZONTAL, 1f, 5, 10)
        })

        setTile(Glyph.WALL_DAMAGE, SimpleTile(this, 4, 3))

        setTile(Glyph.SHALLOW_WATER, WaterTile(this).apply {
            add(0.3f, 9, 0)
            add(0.4f, 11, 0)
            add(0.3f, 10, 0)
        })
        setTile(Glyph.DEEP_WATER, WaterTile(this).apply {
            add(0.3f, 9, 4)
            add(0.4f, 11, 4)
            add(0.3f, 10, 4)
        })

        setTile(Glyph.LAVA, AnimatedTile(this).apply {
            frameMs = 500
            add(10, 5)
            add(10, 6)
            add(10, 7)
        })

        setTile(Glyph.PORTAL_DOOR, SimpleTile(this, 3, 2))
        setTile(Glyph.PORTAL_CAVE, SimpleTile(this, 0, 8))

        setTile(Glyph.PAVEMENT, VariantsTile(this).apply {
            add(0.5f, 8, 6)
            add(0.5f, 9, 6)
        })

        setTile(Glyph.HIGHWAY_H, HighwayTile(this).apply {
            isHorizontal = true
            add(HighwayTile.Slot.PLAIN, 0.5f, 8, 7)
            add(HighwayTile.Slot.PLAIN, 0.5f, 9, 7)
            add(HighwayTile.Slot.STRIPED, 0.5f, 6, 7)
            add(HighwayTile.Slot.STRIPED, 0.5f, 7, 7)
        })

        setTile(Glyph.HIGHWAY_V, HighwayTile(this).apply {
            isHorizontal = false
            add(HighwayTile.Slot.PLAIN, 0.5f, 8, 7)
            add(HighwayTile.Slot.PLAIN, 0.5f, 9, 7)
            add(HighwayTile.Slot.STRIPED, 0.5f, 6, 6)
            add(HighwayTile.Slot.STRIPED, 0.5f, 7, 6)
        })

        setTile(Glyph.CAVE_ROCKS, SimpleTile(this, 1, 8))
    }
