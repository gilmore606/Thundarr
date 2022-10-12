package world.terrains

import render.tilesets.Glyph

object BrickWall : Terrain(
    Glyph.WALL,
    false,
    false,
    true,
    "You bump into a brick wall."
)
