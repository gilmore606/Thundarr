package world

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.XY
import world.gen.NoisePatches
import world.gen.biomes.Biome
import world.gen.biomes.Blank
import world.gen.habitats.Habitat

// Metadata about how to construct a chunk.  Generated at worldgen.


@Serializable
class ChunkMeta(
    val x: Int = 0,
    val y: Int = 0,
    val height: Int = 0,
    val temperature: Int = 0,
    val riverExits: MutableList<RiverExit> = mutableListOf(),
    val riverBlur: Float = 0f,
    val hasLake: Boolean = false,
    val coasts: MutableList<XY> = mutableListOf(),
    val biome: Biome = Blank,
    val habitat: Habitat = world.gen.habitats.Blank,
    val roadExits: MutableList<RoadExit> = mutableListOf(),
    val trailExits: MutableList<TrailExit> = mutableListOf(),
    val lavaExits: MutableList<LavaExit> = mutableListOf(),
    val variance: Float = 0f,
    var hasCity: Boolean = false,
    var hasVolcano: Boolean = false,
    var cityDistance: Float = 0f,
    var ruinedBuildings: Int = 0,
    var title: String = "the wilderness"
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

@Serializable
class TrailExit(
    var pos: XY,
    var edge: XY,
    var control: XY
)

@Serializable
class LavaExit(
    var pos: XY,
    var edge: XY,
    var width: Int = 4
)

class ChunkScratch(
    var x: Int = 0,
    var y: Int = 0
) {
    var height = -1
    var temperature = 60
    var riverParentX = -1
    var riverParentY = -1
    var riverChildren: MutableList<XY> = mutableListOf()
    var riverDescendantCount = 0
    var riverExits: MutableList<RiverExit> = mutableListOf()
    var riverBlur = 0f
    var hasLake = false
    var dryness = -1
    var coasts: MutableList<XY> = mutableListOf()
    var biome: Biome = Blank
    var habitat: Habitat = world.gen.habitats.Blank
    var roadExits: MutableList<RoadExit> = mutableListOf()
    var trailExits: MutableList<TrailExit> = mutableListOf()
    var lavaExits: MutableList<LavaExit> = mutableListOf()
    var hasCity = false
    var hasVolcano = false
    var cityDistance = 0f
    var ruinedBuildings = 0
    var title = ""
    fun toChunkMeta() = ChunkMeta(
        x = x,
        y = y,
        height = height,
        temperature = temperature,
        riverExits = riverExits,
        riverBlur = riverBlur,
        hasLake = hasLake,
        coasts = coasts,
        biome = biome,
        habitat = habitat,
        variance = NoisePatches.get("metaVariance",x,y).toFloat(),
        roadExits = roadExits,
        trailExits = trailExits,
        lavaExits = lavaExits,
        hasCity = hasCity,
        hasVolcano = hasVolcano,
        cityDistance = cityDistance,
        ruinedBuildings = ruinedBuildings,
        title = title
    )
}
