package world.gen.biomes

import actors.NPC
import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice
import util.Rect
import util.XY
import world.Chunk
import world.gen.AnimalSpawnSource
import world.gen.NoisePatches
import world.gen.cartos.WorldCarto
import world.gen.habitats.Habitat
import world.level.CHUNK_SIZE
import world.terrains.Terrain
import world.terrains.Terrain.Type.*
import world.terrains.Wall

@Serializable
sealed class Biome(
    val mapGlyph: Glyph,
    val baseTerrain: Terrain.Type,
) : AnimalSpawnSource {
    open fun defaultTitle(habitat: Habitat) = "the wilderness"
    open fun trailChance() = 0.1f
    open fun plantDensity() = 1.0f
    open fun cabinChance() = 0.0f
    open fun cavesChance() = 0.0f
    open fun outcroppingChance() = 0.0f
    open fun pondChance() = 0.0f
    open fun ambientSoundDay(): Speaker.Ambience = Speaker.Ambience.OUTDOORDAY
    open fun ambientSoundNight(): Speaker.Ambience = Speaker.Ambience.OUTDOORNIGHT
    open fun canHaveRain() = true
    open fun temperatureBase() = 0
    open fun temperatureAmplitude() = 1f
    open fun riverBankTerrain(x: Int, y: Int): Terrain.Type = if (NoisePatches.get("plantsBasic",x,y) > 0.4f) riverBankAltTerrain(x,y) else baseTerrain
    open fun riverBankAltTerrain(x: Int, y: Int): Terrain.Type = TERRAIN_UNDERGROWTH
    open fun bareTerrain(x: Int, y: Int): Terrain.Type = Terrain.Type.TERRAIN_DIRT
    open fun trailSideTerrain(x: Int, y: Int): Terrain.Type = Terrain.Type.TERRAIN_UNDERGROWTH
    open fun villageWallType() = Terrain.Type.TERRAIN_WOODWALL
    open fun villageFloorType() = Terrain.Type.TERRAIN_WOODFLOOR

    open fun terrainAt(x: Int, y: Int): Terrain.Type = baseTerrain
    open fun fertilityAt(x: Int, y: Int) = NoisePatches.get("plantsBasic", x, y).toFloat()
    open fun postBlendProcess(carto: WorldCarto, dir: Rect) { }
    open fun postProcess(carto: WorldCarto) { }
    open fun placeExtraThings(carto: WorldCarto) { }
    open fun wallsBlockTrails() = true
    open fun metaTravelCost() = 1f
    open fun edgeDistanceThreatFactor() = 0f
    open fun xpValue() = 2

    protected fun setTerrain(carto: WorldCarto, x: Int, y: Int, type: Terrain.Type) {
        if (carto.boundsCheck(x + carto.x0, y + carto.y0)) {
            carto.setTerrain(x + carto.x0, y + carto.y0, type)
            if (wallsBlockTrails() && Terrain.get(type) is Wall) {
                carto.blockTrailAt(x + carto.x0, y + carto.y0)
            }
        }
    }
    protected fun setFlag(carto: WorldCarto, x: Int, y: Int, flag: WorldCarto.CellFlag) {
        if (carto.boundsCheck(x + carto.x0, y + carto.y0)) {
            carto.setFlag(x + carto.x0, y + carto.y0, flag)
        }
    }

    protected fun digLake(carto: WorldCarto, x0: Int, y0: Int, x1: Int, y1: Int) {
        val blob = carto.growOblong(x1-x0, y1-y0)
        carto.printGrid(blob, x0 + carto.x0, y0 + carto.y0, GENERIC_WATER)
        carto.addTrailBlock(x0,y0,x1,y1)
    }

    protected fun addOutcropping(carto: WorldCarto) {
        val w = Dice.range(3, 12)
        val h = Dice.range(3, 12)
        val x0 = Dice.range(3, CHUNK_SIZE - w - 3)
        val y0 = Dice.range(3, CHUNK_SIZE - h - 3)
        val x1 = x0+w-1
        val y1 = y0+h-1
        val blob = carto.growOblong(x1-x0, y1-y0)
        carto.printGrid(blob, x0 + carto.x0, y0 + carto.y0, Terrain.Type.TEMP5)
        carto.fringeTerrain(TEMP5, TERRAIN_ROCKS, Dice.float(0.2f, 0.9f), GENERIC_WATER)
        carto.swapTerrain(TEMP5, TERRAIN_CAVEWALL)
    }

    protected fun addPond(carto: WorldCarto) {
        val width = Dice.range(5, 12)
        val height = Dice.range(5, 12)
        val x = Dice.range(5, CHUNK_SIZE - 20) + carto.x0
        val y = Dice.range(5, CHUNK_SIZE - 20) + carto.y0
        carto.printGrid(carto.growBlob(width, height), x, y, Terrain.Type.TERRAIN_SHALLOW_WATER)
    }

    open fun carveExtraTerrain(carto: WorldCarto) {
        if (Dice.chance(outcroppingChance())) {
            addOutcropping(carto)
        }
        if (Dice.chance(pondChance())) {
            addPond(carto)
        }
    }

    override fun animalSpawnPoint(chunk: Chunk, animalType: NPC.Tag): XY? {
        repeat (200) {
            val x = chunk.x + Dice.zeroTil(CHUNK_SIZE)
            val y = chunk.y + Dice.zeroTil(CHUNK_SIZE)
            // TODO: This should check if the animalType can walk on the square, not the player
            // but we can't right now because we can only call this with an actual NPC, not the tag
            // wat do?
            if (chunk.isWalkableAt(App.player, x, y)) return XY(x,y)
        }
        return null
    }
}

@Serializable
object Blank : Biome(
    Glyph.BLANK,
    BLANK
) {

}

@Serializable
object Ocean : Biome(
    Glyph.MAP_WATER,
    TERRAIN_DEEP_WATER
) {
    override fun defaultTitle(habitat: Habitat) = "ocean"
    override fun trailChance() = 0f
    override fun temperatureBase() = 5
    override fun temperatureAmplitude() = 0.6f
}

@Serializable
object Glacier : Biome(
    Glyph.MAP_GLACIER,
    TERRAIN_DIRT
) {
    override fun defaultTitle(habitat: Habitat) = "glacier"
    override fun canHaveRain() = false
    override fun riverBankAltTerrain(x: Int, y: Int) = TERRAIN_ROCKS
    override fun bareTerrain(x: Int, y: Int) = TERRAIN_ROCKS
    override fun trailSideTerrain(x: Int, y: Int) = TERRAIN_ROCKS
    override fun outcroppingChance() = 0.2f
    override fun xpValue() = 3
    override fun temperatureBase() = -15
}

@Serializable
object Plain : Biome(
    Glyph.MAP_PLAIN,
    TERRAIN_GRASS
) {
    override fun defaultTitle(habitat: Habitat) = habitat.grasslandName()
    override fun riverBankTerrain(x: Int, y: Int) = if (NoisePatches.get("ruinMicro",x,y) > 0.9f) TERRAIN_SWAMP else super.riverBankTerrain(x, y)
    override fun plantDensity() = 0.5f
    override fun cabinChance() = 0.001f
    override fun outcroppingChance() = 0.05f
    override fun pondChance() = 0.02f
    override fun xpValue() = 0

    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val fert = fertilityAt(x, y)
        val variance = NoisePatches.get("metaVariance", x, y).toFloat()
        if (fert > 0.96f + (variance * 0.1f)) {
            return TERRAIN_TEMPERATE_FORESTWALL
        } else if (fert < (variance * 0.004f)) {
            return TERRAIN_DIRT
        }
        return super.terrainAt(x, y)
    }
}

@Serializable
object Hill : Biome(
    Glyph.MAP_HILL,
    TERRAIN_GRASS
) {
    override fun defaultTitle(habitat: Habitat) = "hills"
    override fun trailChance() = 0.2f
    override fun cavesChance() = 0.6f
    override fun cabinChance() = 0.05f
    override fun outcroppingChance() = 0.1f
    override fun pondChance() = 0.005f
    override fun plantDensity() = 0.3f
    override fun riverBankAltTerrain(x: Int, y: Int) = TERRAIN_ROCKS
    override fun villageWallType() = TERRAIN_BRICKWALL
    override fun villageFloorType() = TERRAIN_CAVEFLOOR
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
        carto.fringeTerrain(TERRAIN_CAVEWALL, TERRAIN_ROCKS, 0.7f, GENERIC_WATER)
        repeat (2) { carto.varianceFuzzTerrain(TERRAIN_ROCKS, TERRAIN_CAVEWALL) }
    }
}

@Serializable
object ForestHill : Biome(
    Glyph.MAP_FORESTHILL,
    TERRAIN_GRASS
) {
    override fun defaultTitle(habitat: Habitat) = habitat.forestHillName()
    override fun ambientSoundDay() = Speaker.Ambience.FOREST
    override fun ambientSoundNight() = Speaker.Ambience.FOREST
    override fun trailChance() = 0.2f
    override fun cabinChance() = 0.1f
    override fun cavesChance() = 0.4f
    override fun outcroppingChance() = 0.01f
    override fun pondChance() = 0.05f
    override fun plantDensity() = 0.7f
    override fun riverBankAltTerrain(x: Int, y: Int) = TERRAIN_ROCKS
    override fun villageWallType() = if (Dice.flip()) TERRAIN_BRICKWALL else TERRAIN_WOODWALL
    override fun villageFloorType() = if (Dice.chance(0.7f)) TERRAIN_WOODFLOOR else TERRAIN_CAVEFLOOR
    override fun metaTravelCost() = 2f
    override fun edgeDistanceThreatFactor() = 2f
    override fun xpValue() = 4
    override fun temperatureAmplitude() = 0.7f

    override fun fertilityAt(x: Int, y: Int) = super.fertilityAt(x, y) -
            (NoisePatches.get("mountainShapes", x, y) * 0.7f + NoisePatches.get("extraForest", x, y) * 3f).toFloat()

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
        carto.fringeTerrain(TERRAIN_CAVEWALL, TERRAIN_ROCKS, 0.6f, TERRAIN_TEMPERATE_FORESTWALL)
        carto.fringeTerrain(TERRAIN_TEMPERATE_FORESTWALL, TERRAIN_UNDERGROWTH, 0.6f, TERRAIN_CAVEWALL)
        carto.varianceFuzzTerrain(TERRAIN_ROCKS, TERRAIN_CAVEWALL)
        carto.varianceFuzzTerrain(TERRAIN_UNDERGROWTH, TERRAIN_TEMPERATE_FORESTWALL)
    }
}

@Serializable
object Mountain : Biome(
    Glyph.MAP_MOUNTAIN,
    TERRAIN_DIRT
) {
    override fun defaultTitle(habitat: Habitat) = "mountains"
    override fun ambientSoundDay() = Speaker.Ambience.MOUNTAIN
    override fun ambientSoundNight() = Speaker.Ambience.MOUNTAIN
    override fun cabinChance() = 0.05f
    override fun cavesChance() = 0.8f
    override fun pondChance() = 0.01f
    override fun riverBankAltTerrain(x: Int, y: Int) = TERRAIN_ROCKS
    override fun wallsBlockTrails() = true
    override fun plantDensity() = 0.5f
    override fun villageWallType() = TERRAIN_BRICKWALL
    override fun villageFloorType() = if (Dice.chance(0.1f)) TERRAIN_DIRT else
        if (Dice.flip()) TERRAIN_STONEFLOOR else TERRAIN_CAVEFLOOR
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

    override fun riverBankTerrain(x: Int, y: Int) = if (Dice.flip()) Terrain.Type.TERRAIN_GRASS else TERRAIN_DIRT

}

@Serializable
object Swamp : Biome(
    Glyph.MAP_SWAMP,
    TERRAIN_SWAMP
) {
    override fun defaultTitle(habitat: Habitat) = "swamp"
    override fun ambientSoundDay() = Speaker.Ambience.SWAMP
    override fun ambientSoundNight() = Speaker.Ambience.SWAMP
    override fun trailChance() = 0.4f
    override fun cabinChance() = 0.03f
    override fun outcroppingChance() = 0.05f
    override fun pondChance() = 0.1f
    override fun plantDensity() = 1f
    override fun riverBankTerrain(x: Int, y: Int) = TERRAIN_UNDERGROWTH
    override fun bareTerrain(x: Int, y: Int) = TERRAIN_GRASS
    override fun villageFloorType() = if (Dice.flip()) TERRAIN_DIRT else TERRAIN_WOODFLOOR
    override fun metaTravelCost() = 2f
    override fun edgeDistanceThreatFactor() = 2f
    override fun xpValue() = 4
    override fun temperatureBase() = 5
    override fun temperatureAmplitude() = 0.9f

    override fun fertilityAt(x: Int, y: Int) = NoisePatches.get("swampForest", x, y).toFloat()

    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val localThresh = 0.35f + (NoisePatches.get("metaVariance", x, y) * 0.3f).toFloat()
        val fert = NoisePatches.get("swampForest", x, y).toFloat()
        if (fert > localThresh) {
            return Terrain.Type.TERRAIN_TEMPERATE_FORESTWALL
        } else if (fert > localThresh * 0.6f && Dice.chance(localThresh)) {
            return TERRAIN_UNDERGROWTH
        }
        return super.terrainAt(x, y)
    }

    override fun carveExtraTerrain(carto: WorldCarto) {
        super.carveExtraTerrain(carto)
        repeat(Dice.oneTo(9)) {
            val x = Dice.zeroTil(CHUNK_SIZE-13)
            val y = Dice.zeroTil(CHUNK_SIZE-13)
            digLake(carto, x, y, x + Dice.range(7, 12), y + Dice.range(7, 12))
        }
    }
}

@Serializable
object Scrub : Biome(
    Glyph.MAP_SCRUB,
    TERRAIN_GRASS
) {
    override fun defaultTitle(habitat: Habitat) = habitat.scrubName()
    override fun riverBankAltTerrain(x: Int, y: Int) = if (Dice.chance(0.1f)) TERRAIN_ROCKS else TERRAIN_GRASS
    override fun plantDensity() = 0.25f
    override fun cabinChance() = 0.005f
    override fun outcroppingChance() = 0.1f
    override fun pondChance() = 0.001f
    override fun xpValue() = 1
    override fun villageFloorType() = if (Dice.flip()) TERRAIN_DIRT else TERRAIN_WOODFLOOR
    override fun temperatureAmplitude() = 1.15f

    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val fert = fertilityAt(x, y)
        val variance = NoisePatches.get("metaVariance", x, y).toFloat()
        if (fert < 0.1f + (variance * 0.002f)) {
            return TERRAIN_HARDPAN
        } else if (fert < 0.4f + (variance * 0.004f)) {
            return TERRAIN_DIRT
        }
        return super.terrainAt(x, y)
    }
}

@Serializable
object Desert : Biome(
    Glyph.MAP_DESERT,
    TERRAIN_SAND
) {
    override fun defaultTitle(habitat: Habitat) = "desert"
    override fun canHaveRain() = false
    override fun cavesChance() = 0.8f
    override fun cabinChance() = 0.01f
    override fun ambientSoundDay() = Speaker.Ambience.DESERT
    override fun ambientSoundNight() = Speaker.Ambience.DESERT
    override fun riverBankTerrain(x: Int, y: Int) = if (NoisePatches.get("plantsBasic", x, y) > 0.1)
        TERRAIN_GRASS else TERRAIN_HARDPAN
    override fun bareTerrain(x: Int, y: Int) = TERRAIN_HARDPAN
    override fun trailSideTerrain(x: Int, y: Int) = TERRAIN_ROCKS
    override fun plantDensity() = 0.1f
    override fun villageWallType() = if (Dice.flip()) TERRAIN_BRICKWALL else TERRAIN_CAVEWALL
    override fun villageFloorType() = if (Dice.chance(0.2f)) TERRAIN_HARDPAN else TERRAIN_STONEFLOOR
    override fun metaTravelCost() = 0.7f
    override fun edgeDistanceThreatFactor() = 1f
    override fun xpValue() = 3
    override fun temperatureBase() = 12
    override fun temperatureAmplitude() = 1.3f

    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val fert = fertilityAt(x, y)
        val v = NoisePatches.get("desertRocks",x,y).toFloat()
        val variance = NoisePatches.get("metaVariance", x, y).toFloat()
        if (v > 0.45f) return TERRAIN_CAVEWALL
        else if (v > 0.35f && Dice.chance((v - 0.35f) * 10f)) return TERRAIN_ROCKS
        else if (fert > 0.5f + (variance * 0.006f)) return TERRAIN_HARDPAN
        return super.terrainAt(x,y)
    }
}

// Non-map biome for spawning plants
@Serializable
object Cavern : Biome(
    Glyph.BLANK,
    TERRAIN_CAVEFLOOR
) {
    override fun plantDensity() = 0.1f
}

@Serializable
object Beach : Biome(
    Glyph.MAP_DESERT,
    TERRAIN_BEACH
) {

}

@Serializable
object Tundra: Biome(
    Glyph.MAP_PLAIN,
    TERRAIN_DIRT
) {
    override fun defaultTitle(habitat: Habitat) = "tundra"
    override fun canHaveRain() = false
}
