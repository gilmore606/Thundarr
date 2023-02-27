package world

import kotlinx.serialization.Serializable
import util.XY
import world.gen.NoisePatches
import world.gen.biomes.Biome
import world.gen.biomes.Blank
import world.gen.features.*
import world.gen.habitats.Habitat
import kotlin.reflect.KClass

// Metadata about how to construct a chunk.  Generated at worldgen.


@Serializable
class ChunkMeta(
    val x: Int = 0,
    val y: Int = 0,
    val height: Int = 0,
    val temperature: Int = 0,
    val biome: Biome = Blank,
    val habitat: Habitat = world.gen.habitats.Blank,
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

    fun featureOf(ofClass: KClass<out Any>): ChunkFeature? {
        features.forEach { if (ofClass.isInstance(it)) return it }
        return null
    }

    fun coasts(): List<XY> = featureOf(Coastlines::class)?.let {
        (it as Coastlines).exits
    } ?: listOf<XY>()

    fun rivers(): List<Rivers.RiverExit> = featureOf(Rivers::class)?.let {
        (it as Rivers).exits
    } ?: listOf()

    fun highways(): List<Highways.HighwayExit> = featureOf(Highways::class)?.let {
        (it as Highways).exits
    } ?: listOf()

    fun trails(): List<Trails.TrailExit> = featureOf(Trails::class)?.let {
        (it as Trails).exits
    } ?: listOf()
}

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
    var dryness = -1
    var biome: Biome = Blank
    var habitat: Habitat = world.gen.habitats.Blank
    var features: MutableList<ChunkFeature> = mutableListOf()
    var cityDistance = 0f
    var title = ""

    fun toChunkMeta() = ChunkMeta(
        x = x,
        y = y,
        height = height,
        temperature = temperature,
        biome = biome,
        habitat = habitat,
        variance = NoisePatches.get("metaVariance",x,y).toFloat(),
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

    fun addTrailExit(exit: Trails.TrailExit) {
        featureOf(Trails::class)?.also {
            (it as Trails).addExit(exit)
        } ?: run {
            features.add(Trails(mutableListOf(exit)))
        }
    }

    fun addRiverExit(exit: Rivers.RiverExit) {
        featureOf(Rivers::class)?.also {
            (it as Rivers).addExit(exit)
        } ?: run {
            features.add(Rivers(mutableListOf(exit)))
        }
    }

    fun addHighwayExit(exit: Highways.HighwayExit) {
        featureOf(Highways::class)?.also {
            (it as Highways).addExit(exit)
        } ?: run {
            features.add(Highways(mutableListOf(exit)))
        }
    }

    fun rivers(): List<Rivers.RiverExit> = featureOf(Rivers::class)?.let {
        (it as Rivers).exits
    } ?: listOf()

    fun highways(): List<Highways.HighwayExit> = featureOf(Highways::class)?.let {
        (it as Highways).exits
    } ?: listOf()

    fun trails(): List<Trails.TrailExit> = featureOf(Trails::class)?.let {
        (it as Trails).exits
    } ?: listOf()

    fun defaultTitle() = features.firstNotNullOfOrNull { it.cellTitle() } ?: biome.defaultTitle(habitat)
}
