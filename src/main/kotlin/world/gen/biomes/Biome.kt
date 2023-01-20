package world.gen.biomes

import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.*
import util.Dice
import util.Rect
import world.gen.NoisePatches
import world.gen.cartos.WorldCarto
import world.level.CHUNK_SIZE
import world.terrains.Terrain
import world.terrains.Terrain.Type.*
import world.terrains.Undergrowth

@Serializable
sealed class Biome(
    val mapGlyph: Glyph,
    val baseTerrain: Terrain.Type,
) {
    open fun defaultTitle() = "the wilderness"
    open fun canHaveLake() = true
    open fun trailChance() = 0.1f
    open fun plantDensity() = 1.0f
    open fun ambientSoundDay(): Speaker.Ambience = Speaker.Ambience.OUTDOORDAY
    open fun ambientSoundNight(): Speaker.Ambience = Speaker.Ambience.OUTDOORNIGHT
    open fun canHaveRain() = true
    open fun riverBankTerrain(x: Int, y: Int): Terrain.Type = if (NoisePatches.get("plantsBasic",x,y) > 0.4f) riverBankAltTerrain(x,y) else baseTerrain
    open fun riverBankAltTerrain(x: Int, y: Int): Terrain.Type = TERRAIN_UNDERGROWTH
    open fun trailTerrain(x: Int, y: Int): Terrain.Type = Terrain.Type.TERRAIN_DIRT

    open fun terrainAt(x: Int, y: Int): Terrain.Type = baseTerrain
    open fun fertilityAt(x: Int, y: Int) = NoisePatches.get("plantsBasic", x, y).toFloat()
    open fun postBlendProcess(carto: WorldCarto, dir: Rect) { }
    open fun carveExtraTerrain(carto: WorldCarto) { }

    protected fun setTerrain(carto: WorldCarto, x: Int, y: Int, type: Terrain.Type) {
        if (carto.boundsCheck(x + carto.x0, y + carto.y0)) {
            carto.setTerrain(x + carto.x0, y + carto.y0, type)
        }
    }
    protected fun digLake(carto: WorldCarto, x0: Int, y0: Int, x1: Int, y1: Int) {
        val blob = carto.growOblong(x1-x0, y1-y0)
        carto.printGrid(blob, x0 + carto.x0, y0 + carto.y0, GENERIC_WATER)
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
    override fun defaultTitle() = "ocean"
    override fun canHaveLake() = false
    override fun trailChance() = 0f
}

@Serializable
object Glacier : Biome(
    Glyph.MAP_GLACIER,
    TERRAIN_DIRT
) {
    override fun defaultTitle() = "glacier"
    override fun canHaveRain() = false
    override fun riverBankAltTerrain(x: Int, y: Int) = TERRAIN_ROCKS
    override fun trailTerrain(x: Int, y: Int) = TERRAIN_SAND
}

@Serializable
object Plain : Biome(
    Glyph.MAP_PLAIN,
    TERRAIN_GRASS
) {
    override fun defaultTitle() = "grassland"
    override fun riverBankTerrain(x: Int, y: Int) = if (NoisePatches.get("ruinMicro",x,y) > 0.9f) TERRAIN_SWAMP else super.riverBankTerrain(x, y)
    override fun plantDensity() = 0.5f

    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val fert = fertilityAt(x, y)
        val variance = NoisePatches.get("metaVariance", x, y).toFloat()
        if (fert > 0.96f + (variance * 0.1f)) {
            return TERRAIN_FORESTWALL
        } else if (fert < (variance * 0.004f)) {
            return TERRAIN_DIRT
        }
        return super.terrainAt(x, y)
    }

}

@Serializable
object Forest : Biome(
    Glyph.MAP_FOREST,
    TERRAIN_GRASS
) {
    override fun defaultTitle() = "forest"
    override fun ambientSoundDay() = Speaker.Ambience.FOREST
    override fun ambientSoundNight() = Speaker.Ambience.FOREST
    override fun trailChance() = 0.2f
    override fun plantDensity() = 1.4f
    override fun riverBankTerrain(x: Int, y: Int): Terrain.Type = if (fertilityAt(x, y) > 0.6f) TERRAIN_SWAMP else TERRAIN_UNDERGROWTH

    override fun fertilityAt(x: Int, y: Int) = super.fertilityAt(x, y) * 1.5f
    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val fert = NoisePatches.get("plantsBasic", x, y)
        val ef = NoisePatches.get("extraForest", x, y)
        if (fert > 0.7f || ef > 0.2f) {
            return Terrain.Type.TERRAIN_FORESTWALL
        } else if (ef > 0.01f) {
            return Terrain.Type.TERRAIN_UNDERGROWTH
        }
        return super.terrainAt(x, y)
    }
}

@Serializable
object Hill : Biome(
    Glyph.MAP_HILL,
    TERRAIN_GRASS
) {
    override fun defaultTitle() = "hills"
    override fun trailChance() = 0.2f
    override fun plantDensity() = 0.3f
    override fun riverBankAltTerrain(x: Int, y: Int) = TERRAIN_ROCKS

    override fun fertilityAt(x: Int, y: Int) = super.fertilityAt(x, y) - NoisePatches.get("mountainShapes", x, y).toFloat() * 0.6f

    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val v = NoisePatches.get("mountainShapes", x, y)
        if (v > 0.78f) return Terrain.Type.TERRAIN_CAVEWALL
        else if (v < 0.35f) return Terrain.Type.TERRAIN_GRASS
        else return Terrain.Type.TERRAIN_DIRT
    }

    override fun carveExtraTerrain(carto: WorldCarto) {
        carto.fringeTerrain(TERRAIN_CAVEWALL, TERRAIN_ROCKS, 0.7f, GENERIC_WATER)
        repeat (2) { carto.varianceFuzzTerrain(TERRAIN_ROCKS, TERRAIN_CAVEWALL) }
    }
}

@Serializable
object ForestHill : Biome(
    Glyph.MAP_FORESTHILL,
    TERRAIN_GRASS
) {
    override fun defaultTitle() = "wooded hills"
    override fun ambientSoundDay() = Speaker.Ambience.FOREST
    override fun ambientSoundNight() = Speaker.Ambience.FOREST
    override fun trailChance() = 0.2f
    override fun plantDensity() = 0.7f
    override fun riverBankAltTerrain(x: Int, y: Int) = TERRAIN_ROCKS

    override fun fertilityAt(x: Int, y: Int) = super.fertilityAt(x, y) -
            (NoisePatches.get("mountainShapes", x, y) * 0.7f + NoisePatches.get("extraForest", x, y) * 3f).toFloat()

    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        if (NoisePatches.get("extraForest", x, y) > 0.2f) {
            return Terrain.Type.TERRAIN_FORESTWALL
        }
        val v = NoisePatches.get("mountainShapes", x, y)
        if (v > 0.78f) return Terrain.Type.TERRAIN_CAVEWALL
        else if (v < 0.3f) return Terrain.Type.TERRAIN_DIRT
        else return Terrain.Type.TERRAIN_GRASS
    }

    override fun carveExtraTerrain(carto: WorldCarto) {
        carto.fringeTerrain(TERRAIN_CAVEWALL, TERRAIN_ROCKS, 0.6f, TERRAIN_FORESTWALL)
        carto.fringeTerrain(TERRAIN_FORESTWALL, TERRAIN_UNDERGROWTH, 0.6f, TERRAIN_CAVEWALL)
        carto.varianceFuzzTerrain(TERRAIN_ROCKS, TERRAIN_CAVEWALL)
        carto.varianceFuzzTerrain(TERRAIN_UNDERGROWTH, TERRAIN_FORESTWALL)
    }
}

@Serializable
object Mountain : Biome(
    Glyph.MAP_MOUNTAIN,
    TERRAIN_DIRT
) {
    override fun defaultTitle() = "mountains"
    override fun ambientSoundDay() = Speaker.Ambience.MOUNTAIN
    override fun ambientSoundNight() = Speaker.Ambience.MOUNTAIN
    override fun riverBankAltTerrain(x: Int, y: Int) = TERRAIN_ROCKS
    override fun trailTerrain(x: Int, y: Int) = TERRAIN_HARDPAN
    override fun plantDensity() = 0.5f

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
    override fun defaultTitle() = "swamp"
    override fun ambientSoundDay() = Speaker.Ambience.SWAMP
    override fun ambientSoundNight() = Speaker.Ambience.SWAMP
    override fun trailChance() = 0.4f
    override fun plantDensity() = 1f
    override fun trailTerrain(x: Int, y: Int) = TERRAIN_GRASS
    override fun riverBankTerrain(x: Int, y: Int) = TERRAIN_UNDERGROWTH

    override fun fertilityAt(x: Int, y: Int) = NoisePatches.get("swampForest", x, y).toFloat()

    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val localThresh = 0.35f + (NoisePatches.get("metaVariance", x, y) * 0.3f).toFloat()
        val fert = NoisePatches.get("swampForest", x, y).toFloat()
        if (fert > localThresh) {
            return Terrain.Type.TERRAIN_FORESTWALL
        } else if (fert > localThresh * 0.6f && Dice.chance(localThresh)) {
            return TERRAIN_UNDERGROWTH
        }
        return super.terrainAt(x, y)
    }

    override fun carveExtraTerrain(carto: WorldCarto) {
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
    override fun defaultTitle() = "plains"
    override fun riverBankAltTerrain(x: Int, y: Int) = if (Dice.chance(0.1f)) TERRAIN_ROCKS else TERRAIN_GRASS
    override fun trailTerrain(x: Int, y: Int) = TERRAIN_DIRT
    override fun plantDensity() = 0.25f

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
    override fun defaultTitle() = "desert"
    override fun canHaveRain() = false
    override fun ambientSoundDay() = Speaker.Ambience.DESERT
    override fun ambientSoundNight() = Speaker.Ambience.DESERT
    override fun riverBankTerrain(x: Int, y: Int) = if (NoisePatches.get("plantsBasic", x, y) > 0.1)
        TERRAIN_GRASS else TERRAIN_HARDPAN
    override fun plantDensity() = 0.1f

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
    override fun defaultTitle() = "tundra"
    override fun canHaveRain() = false
}

@Serializable
object Suburb: Biome(
    Glyph.MAP_SUBURB,
    TERRAIN_DIRT
) {
    override fun defaultTitle() = "suburban ruins"
    override fun riverBankAltTerrain(x: Int, y: Int) = TERRAIN_GRASS
    override fun trailChance() = 0f
    override fun plantDensity() = 0.2f
}

@Serializable
object Ruins : Biome(
    Glyph.MAP_RUINS,
    Terrain.Type.TERRAIN_PAVEMENT
) {
    override fun defaultTitle() = "urban ruins"
    override fun ambientSoundDay() = Speaker.Ambience.RUINS
    override fun ambientSoundNight() = Speaker.Ambience.RUINS
    override fun riverBankAltTerrain(x: Int, y: Int) = TERRAIN_DIRT
    override fun trailChance() = 0f
    override fun plantDensity() = 0.0f

    override fun carveExtraTerrain(carto: WorldCarto) {
        val gridsize = Dice.range(2, 4)
        val cellsize = CHUNK_SIZE / gridsize
        val variance = cellsize / 6
        val padding = when (gridsize) {
            2 -> 3
            3 -> 2
            else -> 1
        }
        for (ix in 0 until gridsize) {
            for (iy in 0 until gridsize) {
                val x0 = ix * cellsize + padding + Dice.range(1, variance)
                val y0 = iy * cellsize + padding + Dice.range(1, variance)
                val x1 = (ix+1) * cellsize - padding - Dice.range(1, variance)
                val y1 = (iy+1) * cellsize - padding - Dice.range(1, variance)
                if (Dice.chance(0.1f)) {
                    val mid = x0 + (x1 - x0) / 2 - 1
                    carveRuin(carto, x0, y0, x0 + mid - 1, y1)
                    carveRuin(carto, x0 + mid + 1, y0, x1, y1)
                } else if (Dice.chance(0.1f)) {
                    val mid = y0 + (y1 - y0) / 2 - 1
                    carveRuin(carto, x0, y0, x1, y0 + mid - 1)
                    carveRuin(carto, x0, y0 + mid + 1, x1, y1)
                } else if (Dice.chance(0.04f)) {
                    digLake(carto, x0, y0, x1, y1)
                } else if (Dice.chance(0.93f)) {
                    carveRuin(carto, x0, y0, x1, y1)
                }
            }
        }
    }

    private fun carveRuin(carto: WorldCarto, x0: Int, y0: Int, x1: Int, y1: Int) {
        var clear = true
        for (ix in x0 .. x1) {
            for (iy in y0 .. y1) {
                if (listOf(
                        TERRAIN_DEEP_WATER, TERRAIN_SHALLOW_WATER, GENERIC_WATER
                ).contains(carto.getTerrain(ix + carto.x0, iy + carto.y0))) clear = false
            }
        }
        if (!clear) return
        val filled = Dice.chance(0.3f)
        for (ix in x0 .. x1) {
            for (iy in y0 .. y1) {
                val wear = NoisePatches.get("ruinWear", ix + carto.x0, iy + carto.y0).toFloat()
                if (wear < 0.4f && Dice.chance(1.1f - wear * 0.5f)) {
                    val microwear = NoisePatches.get("ruinMicro", ix + carto.x0, iy + carto.y0).toFloat()
                    if (microwear > wear) {
                        if (ix == x0 || ix == x1 || iy == y0 || iy == y1 || filled || microwear > 0.6f) {
                            if (Dice.chance(0.9f)) setTerrain(carto, ix, iy, Terrain.Type.TERRAIN_BRICKWALL)
                        } else {
                            setTerrain(carto, ix, iy, Terrain.Type.TERRAIN_STONEFLOOR)
                        }
                    }
                }
            }
        }
    }

}
