package world.gen.cartos

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
import world.gen.biomes.Biome
import world.gen.biomes.Ocean
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

    val chunkBlendWidth = 6
    val chunkBlendCornerRadius = 4

    suspend fun carveWorldChunk() {
        val meta = App.save.getWorldMeta(x0, y0) ?: ChunkMeta()

        if (meta.biome == Ocean) {

            carveRoom(Rect(x0,y0,x1,y1), 0, Terrain.Type.TERRAIN_DEEP_WATER)

        } else {

//            forEachCell { x, y ->
//                val n = Perlin.noise((x.toDouble() + offset) * scale, y.toDouble() * scale, 59.0) +
//                        Perlin.noise((x.toDouble() + offset) * scale * 0.4, y.toDouble() * scale * 0.4, 114.0) * 0.7
//                if (n > fullness * scale - Dice.float(0f, 0.18f).toDouble()) {
//                    carve(x, y, 0, Terrain.Type.TERRAIN_DIRT)
//                } else {
//                    carve(x, y, 0, Terrain.Type.TERRAIN_GRASS)
//                }
//                val n2 = Perlin.noise(x * 0.02, y * 0.03, 8.12) +
//                        Perlin.noise(x * 0.041, y * 0.018, 11.17) * 0.8
//                if (n2 > 0.02) {
//                    carve(x, y, 0, Terrain.Type.TERRAIN_FORESTWALL)
//                }
//            }

            // Carve base terrain (blend chunk edges if necessary)
            var hasBlends = false
            val neighborMetas = mutableMapOf<XY,ChunkMeta?>().apply {
                DIRECTIONS.forEach { dir ->
                    val neighbor = App.save.getWorldMeta(x0 + dir.x * CHUNK_SIZE, y0 + dir.y * CHUNK_SIZE)
                    if (neighbor == null || neighbor.biome == Ocean || neighbor.biome == meta.biome) {
                        set(dir, null)
                    } else {
                        set(dir, neighbor)
                        hasBlends = true
                    }
                }
            }
            if (!hasBlends) {
                forEachCell { x,y ->
                    carve(x, y, 0, meta.biome.terrainAt(x,y))
                }
            } else {
                val blendMap = Array(CHUNK_SIZE) { Array(CHUNK_SIZE) { mutableSetOf<Pair<Biome, Float>>().apply {
                    add(Pair(meta.biome, 1f))
                } } }
                // Blend sides
                for (i in 0 until CHUNK_SIZE) {
                    for (j in 0 until chunkBlendWidth) {
                        val neighborWeight = 1f - (j * (1f / chunkBlendWidth))
                        neighborMetas[NORTH]?.also { blendMap[i][j].add(Pair(it.biome, neighborWeight)) }
                        neighborMetas[SOUTH]?.also { blendMap[i][CHUNK_SIZE-j-1].add(Pair(it.biome, neighborWeight)) }
                        neighborMetas[WEST]?.also { blendMap[j][i].add(Pair(it.biome, neighborWeight)) }
                        neighborMetas[EAST]?.also { blendMap[CHUNK_SIZE-j-1][i].add(Pair(it.biome, neighborWeight)) }
                    }
                }
                // Blend corners
                val maxWidth = chunkBlendWidth + chunkBlendCornerRadius
                for (i in 0 until maxWidth) {
                    for (j in 0 until maxWidth) {
                        val neighborWeight = 1f - (i+j).toFloat()/(maxWidth*2).toFloat()
                        val outsideCornerWeight = kotlin.math.max(0f, neighborWeight - 0.5f)
                        neighborMetas[NORTHWEST]?.also { blendMap[i][j].addIfHeavier(it.biome,
                            if (neighborMetas[NORTH]?.biome == it.biome || neighborMetas[WEST]?.biome == it.biome) neighborWeight else outsideCornerWeight) }
                        neighborMetas[NORTHEAST]?.also { blendMap[CHUNK_SIZE-i-1][j].addIfHeavier(it.biome,
                            if (neighborMetas[NORTH]?.biome == it.biome || neighborMetas[EAST]?.biome == it.biome) neighborWeight else outsideCornerWeight) }
                        neighborMetas[SOUTHWEST]?.also { blendMap[i][CHUNK_SIZE-j-1].addIfHeavier(it.biome,
                            if (neighborMetas[SOUTH]?.biome == it.biome || neighborMetas[WEST]?.biome == it.biome) neighborWeight else outsideCornerWeight) }
                        neighborMetas[SOUTHEAST]?.also { blendMap[CHUNK_SIZE-i-1][CHUNK_SIZE-j-1].addIfHeavier(it.biome,
                            if (neighborMetas[SOUTH]?.biome == it.biome || neighborMetas[EAST]?.biome == it.biome) neighborWeight else outsideCornerWeight) }
                    }
                }
                // Execute the blend map
                forEachCell { x,y ->
                    val weights = blendMap[x-x0][y-y0]
                    var weightTotal = 0f
                    weights.forEach { weightTotal += it.second }
                    var roll = Dice.float(0f, weightTotal)
                    var biome: Biome? = null
                    weights.forEach {
                        if (biome == null && roll <= it.second) {
                            biome = it.first
                        } else {
                            roll -= it.second
                        }
                    }
                    carve(x, y, 0, (biome ?: meta.biome).terrainAt(x,y))
                }
            }

            // Coast?
            if (meta.coasts.isNotEmpty()) {
                meta.coasts.forEach { edge ->
                    if (edge in CARDINALS) {
                        for (i in 0 until CHUNK_SIZE) {
                            when (edge) {
                                NORTH -> setTerrain(x0+i,y0, Terrain.Type.TERRAIN_DEEP_WATER)
                                SOUTH -> setTerrain(x0+i,y1, Terrain.Type.TERRAIN_DEEP_WATER)
                                WEST -> setTerrain(x0, y0+i, Terrain.Type.TERRAIN_DEEP_WATER)
                                EAST -> setTerrain(x1, y0+i, Terrain.Type.TERRAIN_DEEP_WATER)
                            }
                        }
                    } else {
                        when (edge) {
                            NORTHWEST -> {
                                setTerrain(x0,y0, Terrain.Type.TERRAIN_DEEP_WATER)
                                setTerrain(x0+1,y0+1, Terrain.Type.TERRAIN_DEEP_WATER)
                            }
                            NORTHEAST -> {
                                setTerrain(x1,y0, Terrain.Type.TERRAIN_DEEP_WATER)
                                setTerrain(x1-1,y0+1, Terrain.Type.TERRAIN_DEEP_WATER)
                            }
                            SOUTHWEST -> {
                                setTerrain(x0,y1, Terrain.Type.TERRAIN_DEEP_WATER)
                                setTerrain(x0+1,y1-1, Terrain.Type.TERRAIN_DEEP_WATER)
                            }
                            SOUTHEAST -> {
                                setTerrain(x1,y1, Terrain.Type.TERRAIN_DEEP_WATER)
                                setTerrain(x1-1,y1-1, Terrain.Type.TERRAIN_DEEP_WATER)
                            }
                        }
                    }
                }
            }
            repeat (4) { fuzzTerrain(Terrain.Type.TERRAIN_DEEP_WATER, 0.3f) }
            fringeTerrain(Terrain.Type.TERRAIN_DEEP_WATER, Terrain.Type.TERRAIN_SHALLOW_WATER, 1f)
            repeat (2) { fuzzTerrain(Terrain.Type.TERRAIN_SHALLOW_WATER, 0.4f) }
            fringeTerrain(Terrain.Type.TERRAIN_SHALLOW_WATER, Terrain.Type.TERRAIN_BEACH, 1f, Terrain.Type.TERRAIN_DEEP_WATER)
            repeat (2) { fuzzTerrain(Terrain.Type.TERRAIN_BEACH, 0.5f, Terrain.Type.TERRAIN_SHALLOW_WATER) }

            // River?
            when (meta.riverExits.size) {
                0 -> { }
                1 -> {
                    val start = dirToEdge(meta.riverExits[0].edge, meta.riverExits[0].offset)
                    val end = XY(Dice.range(CHUNK_SIZE / 4, (CHUNK_SIZE / 4 * 3)), Dice.range(CHUNK_SIZE / 4, (CHUNK_SIZE / 4 * 3)))
                    drawRiver(start, end, meta.riverExits[0].width, 1, meta.riverWiggle)
                }
                2 -> {
                    val start1 = dirToEdge(meta.riverExits[0].edge, meta.riverExits[0].offset)
                    val start2 = dirToEdge(meta.riverExits[1].edge, meta.riverExits[1].offset)
                    val variance = ((CHUNK_SIZE / 2) * meta.riverWiggle).toInt()
                    val centerX = (start1.x + start2.x) / 2 + Dice.zeroTil(variance) - (variance / 2)
                    val centerY = (start1.y + start2.y) / 2 + Dice.zeroTil(variance) - (variance / 2)
                    val centerWidth = (meta.riverExits[0].width + meta.riverExits[1].width) / 2
                    val end = XY(centerX, centerY)
                    drawRiver(start1, end, meta.riverExits[0].width, centerWidth, meta.riverWiggle)
                    drawRiver(start2, end, meta.riverExits[1].width, centerWidth, meta.riverWiggle)
                }
                else -> {
                    val variance = ((CHUNK_SIZE / 2) * meta.riverWiggle).toInt()
                    val centerX = (CHUNK_SIZE / 2) + Dice.zeroTil(variance) - (variance / 2)
                    val centerY = (CHUNK_SIZE / 2) + Dice.zeroTil(variance) - (variance / 2)
                    val centerWidth = meta.riverExits.maxOf { it.width }
                    val end = XY(centerX, centerY)
                    meta.riverExits.forEach { exit ->
                        val start = dirToEdge(exit.edge, exit.offset)
                        drawRiver(start, end, exit.width, centerWidth, meta.riverWiggle)
                    }
                }
            }
            fuzzTerrain(Terrain.Type.GENERIC_WATER, meta.riverBlur * 0.4f)
            fringeTerrain(Terrain.Type.GENERIC_WATER, Terrain.Type.TERRAIN_GRASS, meta.riverGrass, Terrain.Type.TERRAIN_SHALLOW_WATER)
            fringeTerrain(Terrain.Type.GENERIC_WATER, Terrain.Type.TERRAIN_DIRT, meta.riverDirt, Terrain.Type.TERRAIN_SHALLOW_WATER)

            deepenWater()

            // Building?
            if (Dice.chance(0.05f) || forStarter) {
                val facing = CARDINALS.random()
                carvePrefab(getPrefab(), Random.nextInt(x0, x1 - 20), Random.nextInt(y0, y1 - 20), facing)
                assignDoor(facing)
            }

        }

        setRoofedInRock()
        setOverlaps()
        //addJunk(forAttract)
        //debugBorders()
    }

    private fun addJunk(forAttract: Boolean) {
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
