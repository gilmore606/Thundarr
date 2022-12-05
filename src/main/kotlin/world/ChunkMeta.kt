package world

import kotlinx.serialization.Serializable
import util.XY

// Metadata about how to construct a chunk.  Generated at worldgen.

@Serializable
enum class Biome {
    OCEAN,
    PLAIN,
    FOREST,
    MOUNTAIN,
    SWAMP,
    DESERT,
    TUNDRA,
    RUIN
}

@Serializable
class ChunkMeta(
    val x: Int = 0,
    val y: Int = 0,
    val height: Int = 0,
    val riverExits: MutableList<RiverExit> = mutableListOf(),
    val riverWiggle: Float = 0f,
    val riverBlur: Float = 0f,
    val riverGrass: Float = 0f,
    val riverDirt: Float = 0f,
    val biome: Biome = Biome.OCEAN
)

@Serializable
class RiverExit(
    var edge: XY,
    var width: Int,
    var offset: Int = -999,
    var otherSide: RiverExit? = null  // only used during scratch metamapper gen
)

class ChunkScratch(
    var x: Int = 0,
    var y: Int = 0
) {
    var height = -1
    var riverParentX = -1
    var riverParentY = -1
    var hasRiverChildren = false
    var riverRun = false
    var riverWiggle = 0f
    var riverBlur = 0f
    var riverGrass = 0f
    var riverDirt = 0f
    var biome = Biome.OCEAN

    var riverExits: MutableList<RiverExit> = mutableListOf()

    fun toChunkMeta() = ChunkMeta(
        x = x,
        y = y,
        height = height,
        riverExits = riverExits,
        riverWiggle = riverWiggle,
        riverBlur = riverBlur,
        riverGrass = riverGrass,
        riverDirt = riverDirt,
        biome = biome
    )
}
