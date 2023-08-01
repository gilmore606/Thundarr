package world.gen.biomes

import actors.actors.NPC
import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Thing
import util.Dice
import world.gen.NoisePatches
import world.gen.cartos.WorldCarto
import world.gen.habitats.*
import world.gen.spawnsets.PlantSet
import world.level.CHUNK_SIZE
import world.terrains.Terrain
import things.Thing.Tag.*
import world.gen.spawnsets.AnimalSet
import actors.actors.NPC.Tag.*

@Serializable
object Swamp : Biome(
    Glyph.MAP_SWAMP,
    Terrain.Type.TERRAIN_SWAMP
) {
    override fun defaultTitle(habitat: Habitat) = "swamp"
    override fun ambientSoundDay() = Speaker.Ambience.SWAMP
    override fun ambientSoundNight() = Speaker.Ambience.SWAMP
    override fun trailChance() = 0.4f
    override fun cabinChance() = 0.03f
    override fun outcroppingChance() = 0.05f
    override fun pondChance() = 0.1f
    override fun plantDensity() = 1f
    override fun riverBankTerrain(x: Int, y: Int) = Terrain.Type.TERRAIN_UNDERGROWTH
    override fun bareTerrain(x: Int, y: Int) = Terrain.Type.TERRAIN_GRASS
    override fun villageFloorType() = if (Dice.flip()) Terrain.Type.TERRAIN_DIRT else Terrain.Type.TERRAIN_WOODFLOOR
    override fun metaTravelCost() = 2f
    override fun edgeDistanceThreatFactor() = 2f
    override fun xpValue() = 4
    override fun temperatureBase() = 5
    override fun temperatureAmplitude() = 0.9f
    override fun weatherBias() = 0.2f

    override fun fertilityAt(x: Int, y: Int) = NoisePatches.get("swampForest", x, y).toFloat()

    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val localThresh = 0.35f + (NoisePatches.get("metaVariance", x, y) * 0.3f).toFloat()
        val fert = NoisePatches.get("swampForest", x, y).toFloat()
        if (fert > localThresh) {
            return Terrain.Type.TERRAIN_TEMPERATE_FORESTWALL
        } else if (fert > localThresh * 0.6f && Dice.chance(localThresh)) {
            return Terrain.Type.TERRAIN_UNDERGROWTH
        }
        return super.terrainAt(x, y)
    }

    override fun carveExtraTerrain(carto: WorldCarto) {
        super.carveExtraTerrain(carto)
        repeat(Dice.oneTo(9)) {
            val x = Dice.zeroTil(CHUNK_SIZE -13)
            val y = Dice.zeroTil(CHUNK_SIZE -13)
            digLake(carto, x, y, x + Dice.range(7, 12), y + Dice.range(7, 12))
        }
    }

    override fun plantSet(habitat: Habitat) = when (habitat) {
        is Garden -> GardenPlants.set
        is AlpineA, AlpineB -> SwampPlantsAlpine.set
        is TemperateA, TemperateB -> SwampPlantsTemperate.set
        is TropicalA, TropicalB -> SwampPlantsTropical.set
        else -> null
    }

    override fun animalSet(habitat: Habitat) = SwampAnimals.set
}

object SwampPlantsAlpine {
    val set = PlantSet().apply {
        add(1f, SPRUCETREE, 0.75f)
        add(1f, DEADTREE, 0.7f)
        add(1f, WILDFLOWERS)
        add(1f, BLUEBELLS)
        add(0.3f, FOOLSLEAF)
    }
}

object SwampPlantsTemperate {
    val set = PlantSet().apply {
        add(1f, MAPLETREE, 0.75f)
        add(1f, BIRCHTREE, 0.6f)
        add(1f, DEADTREE, 0.7f)
        add(0.4f, APPLETREE, 0.7f, only = TropicalA)
        add(0.4f, PEARTREE, 0.7f, only = TropicalB)
        add(1f, BERRYBUSH, 0.3f, 0.8f)
        add(1f, WILDFLOWERS)
        add(1f, BLUEBELLS)
        add(1f, LACEMOSS, 0f, 0.8f)
        add(0.3f, FOOLSLEAF)
    }
}

object SwampPlantsTropical {
    val set = PlantSet().apply {
        add(1f, PALMTREE, 0.75f)
        add(0.5f, DEADTREE, 0.7f)
        add(1f, BERRYBUSH, 0.3f, 0.8f)
        add(1f, WILDFLOWERS)
        add(1f, BLUEBELLS)
        add(1f, LACEMOSS, 0f, 0.8f)
    }
}

object SwampAnimals {
    val set = AnimalSet().apply {
        add(1f, TORTLE)
        add(0.5f, TORTLE_BULL)
        add(1f, TORTLE_YOUNG)
        add(2f, FROG)
        add(1f, LAMPREY)
        add(1f, SALAMAN)
    }
}