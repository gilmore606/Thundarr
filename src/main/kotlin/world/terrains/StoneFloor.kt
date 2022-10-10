package world.terrains

import kotlinx.serialization.Serializable
import util.Glyph.*

@Serializable
class StoneFloor : Terrain(
    FLOOR,
    true,
    true,
    false
)
