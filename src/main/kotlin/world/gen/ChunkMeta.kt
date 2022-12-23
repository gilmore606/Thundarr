package world

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.XY
import world.gen.NoisePatches
import world.gen.biomes.Biome
import world.gen.biomes.Blank

// Metadata about how to construct a chunk.  Generated at worldgen.


@Serializable
class ChunkMeta(
    val x: Int = 0,
    val y: Int = 0,
    val height: Int = 0,
    val riverExits: MutableList<RiverExit> = mutableListOf(),
    val riverBlur: Float = 0f,
    val riverGrass: Float = 0f,
    val riverDirt: Float = 0f,
    val hasLake: Boolean = false,
    val coasts: MutableList<XY> = mutableListOf(),
    val biome: Biome = Blank,
    val roadExits: MutableList<RoadExit> = mutableListOf(),
    val variance: Float = 0f
)

@Serializable
class RiverExit(
    var pos: XY,
    var edge: XY,
    var width: Int = 4,
    var control: XY
)

@Serializable
class RoadExit(
    var edge: XY,
    var width: Int = 2
)

class ChunkScratch(
    var x: Int = 0,
    var y: Int = 0
) {
    var height = -1
    var riverParentX = -1
    var riverParentY = -1
    var riverChildren: MutableList<XY> = mutableListOf()
    var riverDescendantCount = 0
    var riverExits: MutableList<RiverExit> = mutableListOf()
    var riverBlur = 0f
    var riverGrass = 0f
    var riverDirt = 0f
    var hasLake = false
    var dryness = -1
    var coasts: MutableList<XY> = mutableListOf()
    var biome: Biome = Blank
    var roadExits: MutableList<RoadExit> = mutableListOf()

    fun toChunkMeta() = ChunkMeta(
        x = x,
        y = y,
        height = height,
        riverExits = riverExits,
        riverBlur = riverBlur,
        riverGrass = riverGrass,
        riverDirt = riverDirt,
        hasLake = hasLake,
        coasts = coasts,
        biome = biome,
        variance = NoisePatches.get("metaVariance",x,y).toFloat(),
        roadExits = roadExits
    )
}
