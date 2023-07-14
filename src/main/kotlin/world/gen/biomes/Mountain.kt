package world.gen.biomes

import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice
import world.gen.NoisePatches
import world.terrains.Terrain
import things.Thing.Tag.*
import world.gen.habitats.*
import world.gen.spawnsets.PlantSet

@Serializable
object Mountain : Biome(
    Glyph.MAP_MOUNTAIN,
    Terrain.Type.TERRAIN_DIRT
) {
    override fun defaultTitle(habitat: Habitat) = "mountains"
    override fun ambientSoundDay() = Speaker.Ambience.MOUNTAIN
    override fun ambientSoundNight() = Speaker.Ambience.MOUNTAIN
    override fun cabinChance() = 0.05f
    override fun cavesChance() = 0.8f
    override fun pondChance() = 0.01f
    override fun riverBankAltTerrain(x: Int, y: Int) = Terrain.Type.TERRAIN_ROCKS
    override fun wallsBlockTrails() = true
    override fun plantDensity() = 0.5f
    override fun villageWallType() = Terrain.Type.TERRAIN_BRICKWALL
    override fun villageFloorType() = if (Dice.chance(0.1f)) Terrain.Type.TERRAIN_DIRT else
        if (Dice.flip()) Terrain.Type.TERRAIN_STONEFLOOR else Terrain.Type.TERRAIN_CAVEFLOOR
    override fun metaTravelCost() = 3f
    override fun edgeDistanceThreatFactor() = 3f
    override fun xpValue() = 5
    override fun temperatureBase() = -8
    override fun temperatureAmplitude() = 1.2f

    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val v = NoisePatches.get("mountainShapes", x, y).toFloat()
        if (v > 0.53f) return Terrain.Type.TERRAIN_CAVEWALL
        else if (NoisePatches.get("ruinMicro",x,y) > NoisePatches.get("metaVariance", x / 10, y / 10) * 2.5f) return Terrain.Type.TERRAIN_CAVEWALL
        else if (v < 0.2f) return Terrain.Type.TERRAIN_DIRT
        else if (Dice.chance(1f - v * 2f)) return Terrain.Type.TERRAIN_DIRT
        else return Terrain.Type.TERRAIN_ROCKS
    }

    override fun riverBankTerrain(x: Int, y: Int) = if (Dice.flip()) Terrain.Type.TERRAIN_GRASS else Terrain.Type.TERRAIN_DIRT

    override fun plantSet(habitat: Habitat) = when (habitat) {
        is Garden -> GardenPlants.set
        is AlpineA, AlpineB -> MountainPlantsAlpine.set
        is TemperateA, TemperateB -> MountainPlantsTemperate.set
        is TropicalA, TropicalB -> MountainPlantsTropical.set
        else -> null
    }
}

object MountainPlantsAlpine {
    val set = PlantSet().apply {
        add(1f, DEADTREE, 0.5f)
        add(0.1f, PINETREE, 0.7f)
        add(1f, BOULDER, 0f, 0.8f)
        add(0.4f, ROCK, 0f, 0.8f)
        add(0.1f, SAGEBUSH, 0.5f)
        add(0.05f, BALMMOSS, 0.5f)
        add(0.1f, WIZARDCAP_MYCELIUM, 0.5f)
    }
}

object MountainPlantsTemperate {
    val set = PlantSet().apply {
        add(1f, DEADTREE, 0.5f)
        add(0.1f, PINETREE, 0.7f)
        add(1f, BOULDER, 0f, 0.8f)
        add(0.4f, ROCK, 0f, 0.8f)
        add(0.3f, SAGEBUSH, 0.5f)
        add(0.2f, BALMMOSS, 0.5f)
        add(0.3f, WIZARDCAP_MYCELIUM, 0.5f)
    }
}

object MountainPlantsTropical {
    val set = PlantSet().apply {
        add(1f, DEADTREE, 0.5f)
        add(0.5f, PINETREE, 0.6f)
        add(0.3f, TEAKTREE, 0.75f)
        add(1f, BOULDER, 0f, 0.8f)
        add(0.4f, ROCK, 0f, 0.8f)
        add(0.3f, THORNBUSH, 0.5f)
        add(0.1f, BALMMOSS, 0.5f)
        add(0.3f, BLOODCAP_MYCELIUM, 0.5f)
    }
}
