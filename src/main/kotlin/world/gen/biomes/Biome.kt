package world.gen.biomes

import actors.NPC
import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice
import util.Rect
import util.XY
import util.log
import world.Chunk
import world.gen.AnimalSpawnSource
import world.gen.NoisePatches
import world.gen.cartos.WorldCarto
import world.gen.habitats.Habitat
import world.gen.spawnsets.PlantSet
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
    open fun cabinChance() = 0.0f
    open fun cavesChance() = 0.0f
    open fun outcroppingChance() = 0.0f
    open fun pondChance() = 0.0f
    open fun ambientSoundDay(): Speaker.Ambience = Speaker.Ambience.OUTDOORDAY
    open fun ambientSoundNight(): Speaker.Ambience = Speaker.Ambience.OUTDOORNIGHT
    open fun canHaveRain() = true
    open fun temperatureBase() = 0
    open fun temperatureAmplitude() = 1f

    open fun terrainAt(x: Int, y: Int): Terrain.Type = baseTerrain
    open fun riverBankTerrain(x: Int, y: Int): Terrain.Type = if (NoisePatches.get("plantsBasic",x,y) > 0.4f) riverBankAltTerrain(x,y) else baseTerrain
    open fun riverBankAltTerrain(x: Int, y: Int): Terrain.Type = TERRAIN_UNDERGROWTH
    open fun bareTerrain(x: Int, y: Int): Terrain.Type = Terrain.Type.TERRAIN_DIRT
    open fun trailSideTerrain(x: Int, y: Int): Terrain.Type = Terrain.Type.TERRAIN_UNDERGROWTH
    open fun villageWallType() = Terrain.Type.TERRAIN_WOODWALL
    open fun villageFloorType() = Terrain.Type.TERRAIN_WOODFLOOR

    open fun plantDensity() = 1.0f
    open fun fertilityAt(x: Int, y: Int) = NoisePatches.get("plantsBasic", x, y).toFloat()
    open fun plantSet(habitat: Habitat): PlantSet? = null

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
        carto.printGrid(carto.growBlob(width, height), x, y, Terrain.Type.GENERIC_WATER)
    }

    open fun carveExtraTerrain(carto: WorldCarto) {
        if (Dice.chance(outcroppingChance())) {
            addOutcropping(carto)
        }
        if (Dice.chance(pondChance())) {
            log.info("digging random pond!")
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
