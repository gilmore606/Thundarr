package world.cartos

import App
import actors.Herder
import actors.MuskOx
import actors.Ox
import actors.Wolfman
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import things.*
import util.*
import world.*
import world.level.CHUNK_SIZE
import world.level.Level
import world.persist.LevelKeeper
import world.terrains.PortalDoor
import world.terrains.Terrain
import java.lang.Integer.max
import java.lang.Math.abs
import java.lang.Math.min
import java.lang.RuntimeException
import kotlin.Exception
import kotlin.math.sign
import kotlin.random.Random

class WorldCarto(
    x0: Int,
    y0: Int,
    x1: Int,
    y1: Int,
    chunk: Chunk,
    level: Level,
    val forStarter: Boolean = false
) : Carto(x0, y0, x1, y1, chunk, level) {

    val scale = 0.02
    val fullness = 0.002

    suspend fun carveWorldChunk(offset: Double = 0.0, forAttract: Boolean = false) {
        val meta = App.save.getWorldMeta(x0, y0) ?: throw RuntimeException("No chunk metadata found for $x0 $y0!")

        if (meta.biome == Biome.OCEAN) {

        }

        forEachCell { x, y ->
            val n = Perlin.noise((x.toDouble() + offset) * scale, y.toDouble() * scale, 59.0) +
                    Perlin.noise((x.toDouble() + offset) * scale * 0.4, y.toDouble() * scale * 0.4, 114.0) * 0.7
            if (n > fullness * scale - Dice.float(0f,0.18f).toDouble()) {
                carve(x, y, 0, Terrain.Type.TERRAIN_DIRT)
            } else {
                carve(x, y, 0, Terrain.Type.TERRAIN_GRASS)
            }
            val n2 = Perlin.noise(x * 0.02, y * 0.03, 8.12) +
                    Perlin.noise(x * 0.041, y * 0.018, 11.17) * 0.8
            if (n2 > 0.02) {
                carve(x, y, 0, Terrain.Type.TERRAIN_FORESTWALL)
            }
        }

        // River?
        when (meta.riverExits.size) {
            0 -> { }
            1 -> {
                val start = dirToEdge(meta.riverExits[0].edge, meta.riverExits[0].offset)
                val end = XY(Dice.range(CHUNK_SIZE / 4, (CHUNK_SIZE / 4 * 3)), Dice.range(CHUNK_SIZE / 4, (CHUNK_SIZE / 4 * 3)))
                drawRiver(start, end, meta.riverExits[0].width, 1, meta.riverWiggle)
            }
            2 -> {
                val start = dirToEdge(meta.riverExits[0].edge, meta.riverExits[0].offset)
                val end = dirToEdge(meta.riverExits[1].edge, meta.riverExits[1].offset)
                drawRiver(start, end, meta.riverExits[0].width, meta.riverExits[1].width, meta.riverWiggle)
            }
            else -> {
                val variance = ((CHUNK_SIZE / 2) * meta.riverWiggle).toInt()
                val centerX = (CHUNK_SIZE / 2) + Dice.zeroTil(variance) - (variance / 2)
                val centerY = (CHUNK_SIZE / 2) + Dice.zeroTil(variance) - (variance / 2)
                val end = XY(centerX, centerY)
                meta.riverExits.forEach { exit ->
                    val start = dirToEdge(exit.edge, exit.offset)
                    drawRiver(start, end, exit.width, exit.width, meta.riverWiggle)
                }
            }
        }

        // Building?
        if (Dice.chance(0.05f) || forStarter) {
            val facing = CARDINALS.random()
            carvePrefab(getPrefab(), Random.nextInt(x0, x1 - 20), Random.nextInt(y0, y1 - 20), facing)
            assignDoor(facing)
        }

        for (x in 0 until width) {
            for (y in 0 until height) {
                if (isWalkableAt(x + this.x0, y + this.y0)) {
                    if (Dice.chance(if (forAttract) 0.010f else 0.005f)) {
                        KtxAsync.launch {
                            if (Dice.chance(0.8f)) {
                                (if (Dice.chance(0.7f)) Ox() else MuskOx()).spawnAt(level, x + x0, y + y0)
                            } else if (Dice.flip()) {
                                Wolfman().spawnAt(level, x + x0, y + y0)
                            } else {
                                Herder().spawnAt(level, x + x0, y + y0)
                            }
                        }
                    }
                    val n = Perlin.noise(x * 0.04, y * 0.04, 0.01) +
                            Perlin.noise(x * 0.7, y * 0.4, 1.5) * 0.5
                    if (Dice.chance(n.toFloat() * 0.7f)) {
                        addThing(x + this.x0, y + this.y0, if (Dice.chance(0.93f)) OakTree() else DeadTree())
                        if (Dice.chance(0.2f)) {
                            var clear = true
                            CARDINALS.forEach { dir ->
                                if (chunk.thingsAt(x + dir.x + this.x0,y + dir.y + this.y0).size > 0) {
                                    clear = false
                                }
                            }
                            if (clear) {
                                val dir = CARDINALS.random()
                                try {

                                    addThing(x + this.x0 + dir.x, y + this.y0 + dir.y, when (Random.nextInt(4)) {
                                        0 -> Apple()
                                        1 -> Axe()
                                        2 -> Pear()
                                        3 -> Pickaxe()
                                        else -> EnergyDrink()
                                    })
                                } catch (_: Exception) { }
                            }
                        }
                    }
                }
            }
        }

        setRoofedInRock()
        setOverlaps()
        //debugBorders()
    }

    private fun assignDoor(facing: XY) {
        if (forStarter) {
            log.info("Looking for door for starter dungeon...")
        }
        forEachCell { x, y ->
            if (getTerrain(x, y) == Terrain.Type.TERRAIN_PORTAL_DOOR) {

                val building = if (forStarter)
                    StarterDungeon().at(x,y).facing(facing)
                else
                    BoringBuilding().at(x,y).facing(facing)

                LevelKeeper.makeBuilding(building)
                chunk.exits.add(Chunk.ExitRecord(
                    Chunk.ExitType.LEVEL, XY(x,y),
                    building.doorMsg(),
                    buildingId = building.id,
                    buildingFirstLevelId = building.firstLevelId
                ))
            }
        }
    }

}
