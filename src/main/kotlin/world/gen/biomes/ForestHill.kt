package world.gen.biomes

import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice
import world.gen.NoisePatches
import world.gen.cartos.WorldCarto
import world.gen.habitats.*
import world.terrains.Terrain
import things.Thing.Tag.*
import world.gen.spawnsets.PlantSet

import actors.actors.NPC.Tag.*
import world.gen.spawnsets.AnimalSet

@Serializable
object ForestHill : Biome(
    Glyph.MAP_FORESTHILL,
    Terrain.Type.TERRAIN_GRASS
) {
    override fun defaultTitle(habitat: Habitat) = habitat.forestHillName()
    override fun ambientSoundDay() = Speaker.Ambience.FOREST
    override fun ambientSoundNight() = Speaker.Ambience.FOREST
    override fun trailChance() = 0.2f
    override fun cabinChance() = 0.1f
    override fun cavesChance() = 0.4f
    override fun outcroppingChance() = 0.1f
    override fun pondChance() = 0.1f
    override fun plantDensity() = 0.7f
    override fun riverBankAltTerrain(x: Int, y: Int) = Terrain.Type.TERRAIN_ROCKS
    override fun villageWallType() = if (Dice.flip()) Terrain.Type.TERRAIN_BRICKWALL else Terrain.Type.TERRAIN_WOODWALL
    override fun villageFloorType() = if (Dice.chance(0.7f)) Terrain.Type.TERRAIN_WOODFLOOR else Terrain.Type.TERRAIN_CAVEFLOOR
    override fun metaTravelCost() = 2f
    override fun edgeDistanceThreatFactor() = 2f
    override fun xpValue() = 4
    override fun temperatureAmplitude() = 0.7f
    override fun maxWeatherRank() = 4

    override fun fertilityAt(x: Int, y: Int) = super.fertilityAt(x, y) -
            (NoisePatches.get("mountainShapes", x, y) * 0.7f + NoisePatches.get("extraForest", x, y) * 5f).toFloat()

    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        if (NoisePatches.get("extraForest", x, y) > 0.2f) {
            return Terrain.Type.TERRAIN_TEMPERATE_FORESTWALL
        }
        val v = NoisePatches.get("mountainShapes", x, y)
        if (v > 0.78f) return Terrain.Type.TERRAIN_CAVEWALL
        else if (v < 0.3f) return Terrain.Type.TERRAIN_DIRT
        else return Terrain.Type.TERRAIN_GRASS
    }

    override fun carveExtraTerrain(carto: WorldCarto) {
        super.carveExtraTerrain(carto)
        carto.fringeTerrain(
            Terrain.Type.TERRAIN_CAVEWALL,
            Terrain.Type.TERRAIN_ROCKS, 0.6f,
            Terrain.Type.TERRAIN_TEMPERATE_FORESTWALL
        )
        carto.fringeTerrain(
            Terrain.Type.TERRAIN_TEMPERATE_FORESTWALL,
            Terrain.Type.TERRAIN_UNDERGROWTH, 0.6f,
            Terrain.Type.TERRAIN_CAVEWALL
        )
        carto.varianceFuzzTerrain(Terrain.Type.TERRAIN_ROCKS, Terrain.Type.TERRAIN_CAVEWALL)
        carto.varianceFuzzTerrain(Terrain.Type.TERRAIN_UNDERGROWTH, Terrain.Type.TERRAIN_TEMPERATE_FORESTWALL)
    }

    override fun plantSet(habitat: Habitat) = when (habitat) {
        is Garden -> GardenPlants.set
        is AlpineA, AlpineB -> ForestHillPlantsAlpine.set
        is TemperateA, TemperateB -> ForestHillPlantsTemperate.set
        is TropicalA, TropicalB -> ForestHillPlantsTropical.set
        else -> null
    }

    override fun animalSet(habitat: Habitat) = ForestHillAnimals.set
}

object ForestHillPlantsAlpine {
    val set = PlantSet().apply {
        add(1f, PINETREE, 0.5f)
        add(0.2f, DEADTREE, 0.4f)
        add(1f, THORNBUSH)
        add(0.3f, BERRYBUSH, 0.5f)
        add(0.1f, BOULDER)
        add(0.1f, BALMMOSS, 0.2f, 0.6f)
        add(0.2f, BOULDER, 0f, 0.5f)
        add(0.2f, SPECKLED_MYCELIUM)
    }
}

object ForestHillPlantsTemperate {
    val set = PlantSet().apply {
        add(2f, OAKTREE, 0.6f)
        add(1f, MAPLETREE, 0.6f)
        add(0.1f, DEADTREE, 0.4f)
        add(1f, THORNBUSH)
        add(0.5f, BERRYBUSH)
        add(0.2f, HONEYPODBUSH)
        add(1f, WILDFLOWERS)
        add(0.6f, BLUEBELLS)
        add(0.1f, BALMMOSS, 0.2f, 0.6f)
        add(0.2f, LACEMOSS, 0.3f, 0.6f)
        add(0.1f, BOULDER, 0f, 0.5f)
        add(0.1f, SPECKLED_MYCELIUM)
        add(0.02f, BLOODCAP_MYCELIUM)
    }
}

object ForestHillPlantsTropical {
    val set = PlantSet().apply {
        add(2f, PALMTREE, 0.6f)
        add(0.1f, DEADTREE, 0.4f)
        add(1f, THORNBUSH)
        add(0.7f, BERRYBUSH)
        add(0.4f, HONEYPODBUSH)
        add(1f, WILDFLOWERS)
        add(0.6f, BLUEBELLS)
        add(0.4f, POPPIES)
        add(0.2f, LACEMOSS, 0.3f, 0.6f)
        add(0.1f, BOULDER, 0f, 0.5f)
        add(0.1f, SPECKLED_MYCELIUM)
        add(0.1f, BLOODCAP_MYCELIUM)
    }
}

object ForestHillAnimals {
    val set = AnimalSet().apply {
        add(1f, BOAR)
        add(1f, WOOD_SPIDER)
        add(1f, PINCER_BEETLE)
        add(0.5f, GECKOID)
        add(1f, CHARMAN)
        add(0.5f, LOSTRICH)
    }
}
