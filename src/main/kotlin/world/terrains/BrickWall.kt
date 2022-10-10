package world.terrains

import kotlinx.serialization.Serializable
import util.Glyph

@Serializable
class BrickWall : Terrain(
    Glyph.WALL,
    false,
    false,
    true,
    "You bump into a brick wall."
)
