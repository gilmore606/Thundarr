package world.gen.biomes

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import world.gen.NoisePatches
import world.gen.habitats.Habitat
import world.gen.spawnsets.PlantSet
import world.terrains.Terrain
import things.Thing.Tag.*
import world.gen.habitats.*

@Serializable
object Plain : Biome(
    Glyph.MAP_PLAIN,
    Terrain.Type.TERRAIN_GRASS
) {
    override fun defaultTitle(habitat: Habitat) = habitat.grasslandName()
    override fun riverBankTerrain(x: Int, y: Int) =
        if (NoisePatches.get("ruinMicro", x, y) > 0.9f) Terrain.Type.TERRAIN_SWAMP else super.riverBankTerrain(x, y)

    override fun plantDensity() = 0.5f
    override fun cabinChance() = 0.001f
    override fun outcroppingChance() = 0.08f
    override fun pondChance() = 0.04f
    override fun xpValue() = 0

    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val fert = fertilityAt(x, y)
        val variance = NoisePatches.get("metaVariance", x, y).toFloat()
        if (fert > 0.96f + (variance * 0.1f)) {
            return Terrain.Type.TERRAIN_TEMPERATE_FORESTWALL
        } else if (fert < (variance * 0.004f)) {
            return Terrain.Type.TERRAIN_DIRT
        }
        return super.terrainAt(x, y)
    }

    override fun plantSet(habitat: Habitat) = when (habitat) {
        is Garden -> GardenPlants.set
        is AlpineA, AlpineB -> PlainPlantsAlpine.set
        is TemperateA, TemperateB -> PlainPlantsTemperate.set
        is TropicalA, TropicalB -> PlainPlantsTropical.set
        else -> null
    }
}

object PlainPlantsAlpine {
    val set = PlantSet().apply {
        add(0.3f, DEADTREE, 0.5f)
        add(0.4f, PINETREE, 0.7f)
        add(0.4f, SPRUCETREE, 0.6f)

        add(1f, THORNBUSH)
        add(0.1f, BERRYBUSH, 0.6f)

        add(1f, WILDFLOWERS)
        add(0.08f, LIGHTFLOWER, 0.3f, 0.7f)

        add(0.04f, BOULDER, 0f, 0.4f)
    }
}

object PlainPlantsTemperate {
    val set = PlantSet().apply {
        add(0.3f, BIRCHTREE, 0.5f)
        add(0.1f, APPLETREE, 0.6f, only = TemperateA)
        add(0.1f, PEARTREE, 0.7f, only = TemperateB)
        add(0.2f, DEADTREE, 0.5f)

        add(1f, THORNBUSH)
        add(1f, SAGEBUSH)
        add(1f, BERRYBUSH)
        add(0.4f, HONEYPODBUSH)

        add(0.5f, WILDFLOWERS)
        add(0.2f, POPPIES)
        add(0.1f, DANDYLIONS)
        add(0.25f, BLUEBELLS)
        add(0.01f, DEATHFLOWER, 0.2f, only = TemperateA)
        add(0.03f, DEATHFLOWER, 0.2f, only = TemperateB)
        add(0.01f, DREAMFLOWER, 0.2f, only = TemperateA)
        add(0.03f, DREAMFLOWER, 0.2f, only = TemperateB)
        add(0.04f, SUNFLOWER, 0.2f, 0.6f, only = TemperateA)
        add(0.04f, LIGHTFLOWER, 0.3f, 0.7f, only = TemperateB)

        add(0.02f, BOULDER, 0f, 0.4f)
    }
}

object PlainPlantsTropical {
    val set = PlantSet().apply {
        add(0.5f, PEARTREE, 0.6f)
        add(0.5f, PALMTREE, 0.8f)
        add(0.4f, COCONUTTREE, 0.8f)
        add(0.3f, DEADTREE, 0.5f)

        add(0.6f, THORNBUSH)
        add(0.5f, BERRYBUSH)
        add(1f, HONEYPODBUSH)

        add(1f, POPPIES)
        add(0.01f, DEATHFLOWER, 0.2f, only = TropicalA)
        add(0.03f, DEATHFLOWER, 0.2f, only = TropicalB)
        add(0.01f, DREAMFLOWER, 0.2f, only = TropicalA)
        add(0.03f, DREAMFLOWER, 0.2f, only = TropicalB)
    }
}
