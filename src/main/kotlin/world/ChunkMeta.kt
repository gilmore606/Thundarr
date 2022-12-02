package world

import kotlinx.serialization.Serializable
import util.XY

// Metadata about how to construct a chunk.  Generated at worldgen.

@Serializable
class ChunkMeta(
    var x: Int = 0,
    var y: Int = 0,
    var riverExits: MutableList<XY> = mutableListOf(),
    var riverWidth: Int = 0
)
