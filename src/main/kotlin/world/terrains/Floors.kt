package world.terrains

import render.tilesets.Glyph

sealed class Floor(
    type: Terrain.Type,
    glyph: Glyph
) : Terrain(type, glyph, true, true, false)


object StoneFloor : Floor(Type.TERRAIN_STONEFLOOR, Glyph.STONE_FLOOR)

object Dirt : Floor(Type.TERRAIN_DIRT, Glyph.DIRT)

object Grass : Floor(Type.TERRAIN_GRASS, Glyph.GRASS)
