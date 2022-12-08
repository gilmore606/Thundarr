package world

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.XY

// Metadata about how to construct a chunk.  Generated at worldgen.

@Serializable
enum class Biome(
    val mapGlyph: Glyph
) {
    BLANK(Glyph.BLANK),
    OCEAN(Glyph.MAP_WATER),
    GLACIER(Glyph.MAP_GLACIER),
    PLAIN(Glyph.MAP_PLAIN),
    FOREST(Glyph.MAP_FOREST),
    MOUNTAIN(Glyph.MAP_MOUNTAIN),
    SWAMP(Glyph.MAP_SWAMP),
    DESERT(Glyph.MAP_DESERT),
    TUNDRA(Glyph.MAP_PLAIN),
    RUIN(Glyph.MAP_PLAIN)
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
    val coasts: MutableList<XY> = mutableListOf(),
    val biome: Biome = Biome.BLANK
)

@Serializable
class RiverExit(
    var edge: XY,
    var dest: XY,
    var width: Int = 4,
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
    var riverChildren: MutableList<XY> = mutableListOf()
    var riverDescendantCount = 0
    var riverExits: MutableList<RiverExit> = mutableListOf()
    var riverWiggle = 0f
    var riverBlur = 0f
    var riverGrass = 0f
    var riverDirt = 0f
    var dryness = -1
    var heat = -1
    var coasts: MutableList<XY> = mutableListOf()
    var biome = Biome.BLANK

    fun toChunkMeta() = ChunkMeta(
        x = x,
        y = y,
        height = height,
        riverExits = riverExits,
        riverWiggle = riverWiggle,
        riverBlur = riverBlur,
        riverGrass = riverGrass,
        riverDirt = riverDirt,
        coasts = coasts,
        biome = biome
    )
}
