package render.tilesets

import render.tileholders.*

fun TerrainTileSet() =
    TileSet(SpriteSheets.Sheet.TerrainSprites).apply {

        setTile(Glyph.BLANK, SimpleTile(this, 2, 2))

        setTile(Glyph.OCCLUSION_SHADOWS_V, SimpleTile(this, 6, 2))
        setTile(Glyph.OCCLUSION_SHADOWS_H, SimpleTile(this, 7, 2))
        setTile(Glyph.SURF_V, SimpleTile(this, 9, 1))
        setTile(Glyph.SURF_H, SimpleTile(this, 11, 1))

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

        setTile(Glyph.GRASS, VariantsTile(this).apply {
            add(0.4f, 8, 0)
            add(0.4f, 8, 1)
            add(0.2f, 8, 2)
        })

        setTile(Glyph.BRICK_WALL, WallTile(this, Glyph.BRICK_WALL).apply {
            add(WallTile.Slot.TOP, 0.33f, 2, 0)
            add(WallTile.Slot.TOP, 0.33f, 3, 0)
            add(WallTile.Slot.TOP, 0.34f, 4, 0)
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
            add(WallTile.Slot.FULL, 1f, 4, 2)
        })
        setTile(Glyph.WALL_DAMAGE, SimpleTile(this, 4, 3))

        setTile(Glyph.WATER, WaterTile(this).apply {
            add(0.3f, 9, 0)
            add(0.4f, 11, 0)
            add(0.3f, 10, 0)
        })

        setTile(Glyph.PORTAL_DOOR, SimpleTile(this, 3, 2))
    }
