package world.gen.biomes

import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice
import world.gen.NoisePatches
import world.gen.cartos.WorldCarto
import world.gen.habitats.*
import world.gen.spawnsets.PlantSet
import world.terrains.Terrain
import things.Thing.Tag.*

@Serializable
object Forest : Biome(
    Glyph.MAP_FOREST,
    Terrain.Type.TERRAIN_GRASS
) {
    private const val cabinChance = 0.2f

    override fun defaultTitle(habitat: Habitat) = habitat.forestName()
    override fun ambientSoundDay() = Speaker.Ambience.FOREST
    override fun ambientSoundNight() = Speaker.Ambience.FOREST
    override fun trailChance() = 0.2f
    override fun cabinChance() = 0.15f
    override fun outcroppingChance() = 0.05f
    override fun pondChance() = 0.1f
    override fun plantDensity() = 1.4f
    override fun riverBankTerrain(x: Int, y: Int): Terrain.Type = if (fertilityAt(x, y) > 0.6f) Terrain.Type.TERRAIN_SWAMP else Terrain.Type.TERRAIN_UNDERGROWTH
    override fun metaTravelCost() = 2f
    override fun edgeDistanceThreatFactor() = 1f
    override fun xpValue() = 3
    override fun temperatureBase() = 5
    override fun temperatureAmplitude() = 0.7f
    override fun maxWeatherRank() = 4
    override fun weatherBias() = 0.1f

    override fun fertilityAt(x: Int, y: Int) = super.fertilityAt(x, y) * 1.5f
    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val fert = NoisePatches.get("plantsBasic", x, y)
        val ef = NoisePatches.get("extraForest", x, y)
        if (fert > 0.7f || ef > 0.2f) {
            return Terrain.Type.TERRAIN_TEMPERATE_FORESTWALL
        } else if (ef > 0.01f) {
            return Terrain.Type.TERRAIN_UNDERGROWTH
        }
        return super.terrainAt(x, y)
    }

    override fun plantSet(habitat: Habitat) = when (habitat) {
        is Garden -> GardenPlants.set
        is AlpineA, AlpineB -> ForestPlantsAlpine.set
        is TemperateA, TemperateB -> ForestPlantsTemperate.set
        is TropicalA, TropicalB -> ForestPlantsTropical.set
        else -> null
    }
}

object ForestPlantsAlpine {
    val set = PlantSet().apply {
        add(1f, PINETREE, 0.5f)
        add(1f, SPRUCETREE, 0.6f)
        add(0.5f, DEADTREE, 0.4f)
        add(1f, THORNBUSH)
        add(0.3f, BERRYBUSH, 0.5f)
        add(0.1f, BOULDER)
        add(0.1f, BLUEBELLS)
        add(0.1f, BALMMOSS, 0.2f, 0.6f)
    }
}

object ForestPlantsTemperate {
    val set = PlantSet().apply {
        add(2f, OAKTREE, 0.7f)
        add(1f, MAPLETREE, 0.7f)
        add(1f, BIRCHTREE, 0.75f)
        add(0.5f, APPLETREE, 0.6f, only = TemperateA)
        add(0.5f, PEARTREE, 0.6f, only = TemperateB)
        add(0.1f, DEADTREE, 0.4f)
        add(1f, THORNBUSH)
        add(0.5f, BERRYBUSH)
        add(0.2f, HONEYPODBUSH)
        add(1f, WILDFLOWERS)
        add(0.5f, BLUEBELLS)
        add(0.1f, BALMMOSS, 0.2f, 0.6f)
        add(0.2f, LACEMOSS, 0.3f, 0.6f)
    }
}

object ForestPlantsTropical {
    val set = PlantSet().apply {
        add(2f, COCONUTTREE, 0.75f)
        add(1.5f, PALMTREE, 0.75f)
        add(0.1f, DEADTREE, 0.4f)
        add(1f, THORNBUSH)
        add(0.7f, BERRYBUSH)
        add(0.4f, HONEYPODBUSH)
        add(1f, WILDFLOWERS)
        add(0.6f, BLUEBELLS)
        add(0.4f, POPPIES)
        add(0.2f, LACEMOSS, 0.3f, 0.6f)
    }
}
