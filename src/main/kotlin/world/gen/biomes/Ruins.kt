package world.gen.biomes

import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Bonepile
import things.ModernDoor
import things.Trunk
import things.WreckedCar
import util.*
import world.Chunk
import world.gen.NoisePatches
import world.gen.cartos.Carto
import world.gen.cartos.WorldCarto
import world.gen.habitats.Habitat
import world.level.CHUNK_SIZE
import world.terrains.*

import actors.actors.NPC.Tag.*
@Serializable
object Ruins : Biome(
    Glyph.MAP_RUINS,
    Terrain.Type.TERRAIN_PAVEMENT
) {
    private const val ruinTreasureChance = 0.5f
    private fun wreckedCarCount() = Dice.oneTo(4)

    override fun defaultTitle(habitat: Habitat) = "urban ruins"
    override fun ambientSoundDay() = Speaker.Ambience.RUINS
    override fun ambientSoundNight() = Speaker.Ambience.RUINS
    override fun riverBankAltTerrain(x: Int, y: Int) = Terrain.Type.TERRAIN_DIRT
    override fun trailChance() = 0f
    override fun plantDensity() = 0.0f
    override fun metaTravelCost() = 2f
    override fun edgeDistanceThreatFactor() = 3f
    override fun xpValue() = 8
    override fun temperatureBase() = 4
    override fun temperatureAmplitude() = 0.8f

    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val fert = fertilityAt(x, y)
        if (fert < 0.2f) {
            return Terrain.Type.TERRAIN_DIRT
        }
        if (fert < 0.25f && Dice.chance((0.25f - fert) * 3f)) {
            return Terrain.Type.TERRAIN_DIRT
        }
        if (fert > 0.6f && Dice.chance((fert - 0.6f) * 0.3f)) {
            return Terrain.Type.TERRAIN_RUBBLE
        }
        return super.terrainAt(x, y)
    }

    override fun carveExtraTerrain(carto: WorldCarto) {
        super.carveExtraTerrain(carto)
        val gridsize = Dice.range(2, 4)
        val cellsize = CHUNK_SIZE / gridsize
        val variance = cellsize / 6
        val padding = when (gridsize) {
            2 -> 3
            3 -> 2
            else -> 1
        }
        forXY(0,0, gridsize-1,gridsize-1) { ix,iy ->
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

    override fun postProcess(carto: WorldCarto) {
        carto.meta.coasts().forEach { carvePiers(carto, it) }
    }

    private fun carveRuin(carto: WorldCarto, x0: Int, y0: Int, x1: Int, y1: Int) {
        if (x1 <= x0 || y1 <= y0) return
        var clear = true
        forXY(x0,y0, x1,y1) { ix,iy ->
            if (listOf(
                    Terrain.Type.TERRAIN_DEEP_WATER, Terrain.Type.TERRAIN_SHALLOW_WATER, Terrain.Type.GENERIC_WATER
                ).contains(carto.getTerrain(ix + carto.x0, iy + carto.y0))) clear = false
        }
        if (!clear) return
        val filled = Dice.chance(0.3f)
        val intact = !filled && Dice.chance(0.3f)
        forXY(x0,y0, x1,y1) { ix,iy ->
            val wear = if (intact) 0f else NoisePatches.get("ruinWear", ix + carto.x0, iy + carto.y0).toFloat()
            if (wear < 0.4f && Dice.chance(1.1f - wear * 0.5f)) {
                val microwear = if (intact) 0.01f else NoisePatches.get("ruinMicro", ix + carto.x0, iy + carto.y0).toFloat()
                if (microwear > wear) {
                    if (ix == x0 || ix == x1 || iy == y0 || iy == y1 || filled || microwear > 0.6f) {
                        if (intact || Dice.chance(0.9f)) setTerrain(carto, ix, iy, Terrain.Type.TERRAIN_BRICKWALL)
                    } else {
                        setTerrain(carto, ix, iy,
                            if (microwear > 0.5f) Terrain.Type.TERRAIN_RUBBLE else Terrain.Type.TERRAIN_STONEFLOOR)
                    }
                    if (intact) {
                        carto.setRoofed(ix + carto.x0, iy + carto.y0, Chunk.Roofed.INDOOR)
                    }
                }
            }
        }
        if (intact) {
            repeat (Dice.oneTo(3)) {
                val doorDir = CARDINALS.random()
                val doorx = if (doorDir == NORTH || doorDir == SOUTH) {
                    Dice.range(x0+1, x1-1)
                } else {
                    if (doorDir == EAST) x1 else x0
                }
                val doory = if (doorDir == EAST || doorDir == WEST) {
                    Dice.range(y0+1, y1-1)
                } else {
                    if (doorDir == SOUTH) y1 else y0
                }
                setTerrain(carto, doorx, doory, Terrain.Type.TERRAIN_STONEFLOOR)
                carto.setRoofed(doorx + carto.x0, doory + carto.y0, Chunk.Roofed.WINDOW)
                if (Dice.chance(0.7f)) {
                    carto.spawnThing(doorx + carto.x0, doory + carto.y0, ModernDoor())
                }
            }
        }
        if (!filled && Dice.chance(ruinTreasureChance)) {
            var placed = false
            var tries = 0
            while (!placed && tries < 200) {
                tries++
                val tx = Dice.range(x0-1,x1+1) + carto.x0
                val ty = Dice.range(y0-1,y1+1) + carto.y0
                if (carto.boundsCheck(tx,ty) && carto.isWalkableAt(tx,ty)) {
                    val treasure = if (Dice.chance(0.25f)) Trunk() else Bonepile().withDefaultLoot(carto.threatLevel)
                    carto.spawnThing(tx, ty, treasure)
                    placed = true
                }
            }
        }
    }

    private fun carvePiers(carto: WorldCarto, coastDir: XY) {
        if (carto.meta.rivers().hasOneWhere { it.edge == coastDir }) return
        var i = 10
        var clip = Dice.zeroTo(2)
        var width = Dice.oneTo(2)
        var spacing = Dice.oneTo(3)
        var pierTerrain = listOf(Terrain.Type.TERRAIN_WOODFLOOR, Terrain.Type.TERRAIN_PAVEMENT).random()
        while (i < CHUNK_SIZE - 10) {
            var beachDone = false
            for (j in clip..10) {
                for (k in 0 until width) {
                    val x = when (coastDir) {
                        EAST -> CHUNK_SIZE - 1 - j
                        WEST -> j
                        else -> i + k
                    }
                    val y = when (coastDir) {
                        NORTH -> j
                        SOUTH -> CHUNK_SIZE - 1 - j
                        else -> i + k
                    }
                    val t = carto.getTerrain(carto.x0 + x, carto.y0 + y)
                    if (t == Terrain.Type.TERRAIN_SHALLOW_WATER || t == Terrain.Type.TERRAIN_DEEP_WATER) {
                        setTerrain(carto, x, y, pierTerrain)
                    }
                    if (t == Terrain.Type.TERRAIN_BEACH && !beachDone) {
                        setTerrain(carto, x, y, pierTerrain)
                        if (Dice.chance(0.5f)) beachDone = true
                    }
                }
            }
            i += width + spacing
            if (Dice.chance(0.1f)) return
            if (Dice.chance(0.1f)) i += Dice.range(5, 30)
            if (Dice.chance(0.3f)) {
                width = Dice.oneTo(2)
                clip = Dice.zeroTo(3)
                spacing = Dice.oneTo(3)
                pierTerrain = listOf(Terrain.Type.TERRAIN_WOODFLOOR, Terrain.Type.TERRAIN_PAVEMENT).random()
                i += Dice.zeroTo(12)
            }
        }
    }

    override fun placeExtraThings(carto: WorldCarto) {
        repeat (wreckedCarCount()) {
            var placed = false
            while (!placed) {
                val tx = Dice.range(carto.x0, carto.x1)
                val ty = Dice.range(carto.y0, carto.y1)
                val t = Terrain.get(carto.getTerrain(tx, ty))
                if (t is HighwayH || t is HighwayV || t is Pavement) {
                    carto.spawnThing(tx, ty, WreckedCar())
                    placed = true
                }
            }
        }
    }
}
