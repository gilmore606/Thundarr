package world.gen.biomes

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import world.gen.NoisePatches
import world.gen.cartos.WorldCarto
import world.terrains.Terrain
import things.Thing.Tag.*
import world.gen.habitats.*
import world.gen.spawnsets.PlantSet

import actors.actors.NPC.Tag.*
import world.gen.spawnsets.AnimalSet

@Serializable
object Hill : Biome(
    Glyph.MAP_HILL,
    Terrain.Type.TERRAIN_GRASS
) {
    override fun defaultTitle(habitat: Habitat) = "hills"
    override fun trailChance() = 0.2f
    override fun cavesChance() = 0.6f
    override fun cabinChance() = 0.05f
    override fun outcroppingChance() = 0.1f
    override fun pondChance() = 0.005f
    override fun plantDensity() = 0.3f
    override fun riverBankAltTerrain(x: Int, y: Int) = Terrain.Type.TERRAIN_ROCKS
    override fun villageWallType() = Terrain.Type.TERRAIN_BRICKWALL
    override fun villageFloorType() = Terrain.Type.TERRAIN_CAVEFLOOR
    override fun metaTravelCost() = 1.5f
    override fun edgeDistanceThreatFactor() = 1f
    override fun xpValue() = 3
    override fun temperatureBase() = -3
    override fun temperatureAmplitude() = 0.9f

    override fun fertilityAt(x: Int, y: Int) = super.fertilityAt(x, y) - NoisePatches.get("mountainShapes", x, y).toFloat() * 0.6f

    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val v = NoisePatches.get("mountainShapes", x, y)
        if (v > 0.78f) return Terrain.Type.TERRAIN_CAVEWALL
        else if (v < 0.35f) return Terrain.Type.TERRAIN_GRASS
        else return Terrain.Type.TERRAIN_DIRT
    }

    override fun carveExtraTerrain(carto: WorldCarto) {
        super.carveExtraTerrain(carto)
        carto.fringeTerrain(Terrain.Type.TERRAIN_CAVEWALL, Terrain.Type.TERRAIN_ROCKS, 0.7f, Terrain.Type.GENERIC_WATER)
        repeat (2) { carto.varianceFuzzTerrain(Terrain.Type.TERRAIN_ROCKS, Terrain.Type.TERRAIN_CAVEWALL) }
    }

    override fun plantSet(habitat: Habitat) = when (habitat) {
        is Garden -> GardenPlants.set
        is AlpineA, AlpineB -> HillPlantsAlpine.set
        is TemperateA, TemperateB -> HillPlantsTemperate.set
        is TropicalA, TropicalB -> HillPlantsTropical.set
        else -> null
    }

    override fun animalSet(habitat: Habitat) = HillAnimals.set
}

object HillPlantsAlpine {
    val set = PlantSet().apply {
        add(0.1f, SPRUCETREE, 0.75f)
        add(0.1f, PINETREE, 0.8f)
        add(0.1f, DEADTREE, 0.6f)
        add(1f, THORNBUSH)
        add(0.4f, WILDFLOWERS, 0.5f)
        add(0.1f, DANDYLIONS, 0.5f)
        add(0.4f, BOULDER, 0f, 0.5f)
        add(0.1f, ROCK, 0f, 0.5f)
        add(0.05f, WIZARDCAP_MYCELIUM)
    }
}

object HillPlantsTemperate {
    val set = PlantSet().apply {
        add(0.1f, BIRCHTREE, 0.75f)
        add(0.1f, DEADTREE, 0.6f)
        add(1f, THORNBUSH)
        add(0.5f, BERRYBUSH, 0.3f)
        add(0.4f, WILDFLOWERS, 0.5f)
        add(0.1f, DANDYLIONS, 0.5f)
        add(0.4f, BOULDER, 0f, 0.5f)
        add(0.1f, ROCK, 0f, 0.5f)
        add(0.02f, WIZARDCAP_MYCELIUM)
    }
}

object HillPlantsTropical {
    val set = PlantSet().apply {
        add(0.1f, PALMTREE, 0.75f)
        add(0.1f, DEADTREE, 0.6f)
        add(1f, THORNBUSH)
        add(0.5f, BERRYBUSH, 0.3f)
        add(0.5f, HONEYPODBUSH, 0.4f)
        add(0.4f, POPPIES, 0.5f)
        add(0.4f, BOULDER, 0f, 0.5f)
        add(0.1f, ROCK, 0f, 0.5f)
        add(0.1f, PRICKPEAR, 0f, 0.5f)
        add(0.01f, BLOODCAP_MYCELIUM)
    }
}

object HillAnimals {
    val set = AnimalSet().apply {
        add(1f, LOSTRICH)
        add(1f, GECKOID)
        add(1f, CHARMAN)
        add(0.2f, MUSKOX)
        add(0.1f, KILLDAISY)
    }
}
