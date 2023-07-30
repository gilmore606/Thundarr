package world.gen.biomes

import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Thing
import util.Dice
import world.gen.NoisePatches
import world.gen.habitats.Habitat
import world.gen.spawnsets.PlantSet
import world.terrains.Terrain
import things.Thing.Tag.*
import world.gen.habitats.AlpineA
import world.gen.habitats.AlpineB
import world.gen.habitats.Garden

import actors.actors.NPC.Tag.*
import world.gen.spawnsets.AnimalSet

@Serializable
object Desert : Biome(
    Glyph.MAP_DESERT,
    Terrain.Type.TERRAIN_SAND
) {
    override fun defaultTitle(habitat: Habitat) = "desert"
    override fun maxWeatherRank() = 2
    override fun weatherBias() = -0.3f
    override fun cavesChance() = 0.8f
    override fun cabinChance() = 0.01f
    override fun ambientSoundDay() = Speaker.Ambience.DESERT
    override fun ambientSoundNight() = Speaker.Ambience.DESERT
    override fun riverBankTerrain(x: Int, y: Int) = if (NoisePatches.get("plantsBasic", x, y) > 0.1)
        Terrain.Type.TERRAIN_GRASS else Terrain.Type.TERRAIN_HARDPAN
    override fun bareTerrain(x: Int, y: Int) = Terrain.Type.TERRAIN_HARDPAN
    override fun trailSideTerrain(x: Int, y: Int) = Terrain.Type.TERRAIN_ROCKS
    override fun plantDensity() = 0.1f
    override fun villageWallType() = if (Dice.flip()) Terrain.Type.TERRAIN_BRICKWALL else Terrain.Type.TERRAIN_CAVEWALL
    override fun villageFloorType() = if (Dice.chance(0.2f)) Terrain.Type.TERRAIN_HARDPAN else Terrain.Type.TERRAIN_STONEFLOOR
    override fun metaTravelCost() = 0.7f
    override fun edgeDistanceThreatFactor() = 1f
    override fun xpValue() = 3
    override fun temperatureBase() = 12
    override fun temperatureAmplitude() = 1.3f

    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val fert = fertilityAt(x, y)
        val v = NoisePatches.get("desertRocks",x,y).toFloat()
        val variance = NoisePatches.get("metaVariance", x, y).toFloat()
        if (v > 0.45f) return Terrain.Type.TERRAIN_CAVEWALL
        else if (v > 0.35f && Dice.chance((v - 0.35f) * 10f)) return Terrain.Type.TERRAIN_ROCKS
        else if (fert > 0.5f + (variance * 0.006f)) return Terrain.Type.TERRAIN_HARDPAN
        return super.terrainAt(x,y)
    }

    override fun plantSet(habitat: Habitat) = when (habitat) {
        is Garden -> GardenPlants.set
        is AlpineA, AlpineB -> DesertPlantsAlpine.set
        else -> DesertPlants.set
    }

    override fun animalSet(habitat: Habitat) = DesertAnimals.set
}

object DesertPlantsAlpine {
    val set = PlantSet().apply {
        add(1f, BOULDER)
        add(1f, DEADTREE)
        add(0.5f, THORNBUSH, 0.6f)
        add(0.2f, WILDFLOWERS, 0.7f)
        add(0.1f, DANDYLIONS)
        add(0.7f, ROCK, 0f, 0.5f)
    }
}

object DesertPlants {
    val set = PlantSet().apply {
        add(0.1f, PALMTREE, 0.8f)
        add(0.5f, SAGEBUSH, 0.6f)
        add(0.2f, DANDYLIONS)
        add(1f, SAGUARO, 0.7f)
        add(1f, CHOLLA, 0.5f)
        add(1f, PRICKPEAR, 0.5f)
        add(0.7f, BOULDER, 0f, 0.6f)
        add(0.7f, ROCK, 0f, 0.5f)
    }
}

object DesertAnimals {
    val set = AnimalSet().apply {
        add(1f, GRUB)
        add(1f, CACTOID)
        add(1f, SCORPION)
        add(0.3f, GLOCUST)
        add(0.3f, HYENAMAN)
    }
}