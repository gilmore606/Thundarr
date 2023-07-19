package world.gen.biomes

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice
import world.gen.NoisePatches
import world.terrains.Terrain
import things.Thing.Tag.*
import world.gen.habitats.*
import world.gen.spawnsets.PlantSet

@Serializable
object Scrub : Biome(
    Glyph.MAP_SCRUB,
    Terrain.Type.TERRAIN_GRASS
) {
    override fun defaultTitle(habitat: Habitat) = habitat.scrubName()
    override fun riverBankAltTerrain(x: Int, y: Int) = if (Dice.chance(0.1f)) Terrain.Type.TERRAIN_ROCKS else Terrain.Type.TERRAIN_GRASS
    override fun plantDensity() = 0.25f
    override fun cabinChance() = 0.005f
    override fun outcroppingChance() = 0.25f
    override fun pondChance() = 0.001f
    override fun xpValue() = 1
    override fun villageFloorType() = if (Dice.flip()) Terrain.Type.TERRAIN_DIRT else Terrain.Type.TERRAIN_WOODFLOOR
    override fun temperatureAmplitude() = 1.15f
    override fun weatherBias() = -0.1f

    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val fert = fertilityAt(x, y)
        val variance = NoisePatches.get("metaVariance", x, y).toFloat()
        if (fert < 0.1f + (variance * 0.002f)) {
            return Terrain.Type.TERRAIN_HARDPAN
        } else if (fert < 0.4f + (variance * 0.004f)) {
            return Terrain.Type.TERRAIN_DIRT
        }
        return super.terrainAt(x, y)
    }

    override fun plantSet(habitat: Habitat) = when (habitat) {
        is Garden -> GardenPlants.set
        is AlpineA, AlpineB -> ScrubPlantsAlpine.set
        is TemperateA, TemperateB -> ScrubPlantsTemperate.set
        is TropicalA, TropicalB -> ScrubPlantsTropical.set
        else -> null
    }
}

object ScrubPlantsAlpine {
    val set = PlantSet().apply {
        add(0.05f, DEADTREE, 0.5f)
        add(1f, THORNBUSH)
        add(1f, SAGEBUSH)
        add(0.2f, WILDFLOWERS)
        add(0.2f, DANDYLIONS)
        add(0.04f, BOULDER, 0f, 0.4f)
    }
}

object ScrubPlantsTemperate {
    val set = PlantSet().apply {
        add(0.05f, DEADTREE, 0.5f)
        add(1f, THORNBUSH)
        add(1f, SAGEBUSH)
        add(0.8f, CHOLLA, 0f, 0.6f)
        add(0.2f, HONEYPODBUSH)
        add(0.3f, WILDFLOWERS)
        add(0.1f, DANDYLIONS, 0.5f)
        add(0.02f, BOULDER, 0f, 0.4f)
        add(0.01f, DREAMFLOWER, 0.2f, only = TemperateA)
        add(0.04f, LIGHTFLOWER, 0.3f, 0.7f, only = TemperateB)
    }
}

object ScrubPlantsTropical {
    val set = PlantSet().apply {
        add(0.05f, DEADTREE, 0.5f)
        add(1f, THORNBUSH)
        add(1f, SAGEBUSH)
        add(0.8f, CHOLLA, 0f, 0.6f)
        add(0.2f, HONEYPODBUSH)
        add(0.8f, WILDFLOWERS)
        add(1f, POPPIES)
        add(0.04f, LIGHTFLOWER, 0.3f, 0.7f, only = TropicalB)
    }
}
