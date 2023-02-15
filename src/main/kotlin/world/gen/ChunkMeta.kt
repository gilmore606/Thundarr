package world

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.XY
import world.gen.NoisePatches
import world.gen.biomes.Biome
import world.gen.biomes.Blank
import world.gen.features.ChunkFeature
import world.gen.features.LavaFlows
import world.gen.habitats.Habitat
import kotlin.reflect.KClass

// Metadata about how to construct a chunk.  Generated at worldgen.


@Serializable
class ChunkMeta(
    val x: Int = 0,
    val y: Int = 0,
    val height: Int = 0,
    val temperature: Int = 0,
    val riverExits: MutableList<RiverExit> = mutableListOf(),
    val riverBlur: Float = 0f,
    val coasts: MutableList<XY> = mutableListOf(),
    val biome: Biome = Blank,
    val habitat: Habitat = world.gen.habitats.Blank,
    val roadExits: MutableList<RoadExit> = mutableListOf(),
    val trailExits: MutableList<TrailExit> = mutableListOf(),
    val variance: Float = 0f,
    var features: MutableList<ChunkFeature> = mutableListOf(),
    var cityDistance: Float = 0f,
    var title: String = "the wilderness",
    var mapped: Boolean = false
) {

    fun hasFeature(ofClass: KClass<out Any>): Boolean {
        features.forEach { if (ofClass.isInstance(it)) return true }
        return false
    }

}

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
    var dryness = -1
    var coasts: MutableList<XY> = mutableListOf()
    var biome: Biome = Blank
    var habitat: Habitat = world.gen.habitats.Blank
    var roadExits: MutableList<RoadExit> = mutableListOf()
    var trailExits: MutableList<TrailExit> = mutableListOf()
    var features: MutableList<ChunkFeature> = mutableListOf()
    var cityDistance = 0f
    var title = ""

    fun toChunkMeta() = ChunkMeta(
        x = x,
        y = y,
        height = height,
        temperature = temperature,
        riverExits = riverExits,
        riverBlur = riverBlur,
        coasts = coasts,
        biome = biome,
        habitat = habitat,
        variance = NoisePatches.get("metaVariance",x,y).toFloat(),
        roadExits = roadExits,
        trailExits = trailExits,
        features = features,
        cityDistance = cityDistance,
        title = title,
        mapped = false
    )

    fun hasFeature(ofClass: KClass<out Any>): Boolean {
        features.forEach { if (ofClass.isInstance(it)) return true }
        return false
    }

    fun featureOf(ofClass: KClass<out Any>): ChunkFeature? {
        features.forEach { if (ofClass.isInstance(it)) return it }
        return null
    }

    fun removeFeature(ofClass: KClass<out Any>) {
        var found: ChunkFeature? = null
        features.forEach { if (ofClass.isInstance(it)) found = it }
        found?.also { features.remove(it) }
    }

    fun addLavaExit(exit: LavaFlows.LavaExit) {
        featureOf(LavaFlows::class)?.also {
            (it as LavaFlows).addExit(exit)
        } ?: run {
            features.add(LavaFlows(mutableListOf(exit)))
        }
    }
}
