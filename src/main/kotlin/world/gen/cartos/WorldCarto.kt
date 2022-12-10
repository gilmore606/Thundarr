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
import world.terrains.Terrain.Type.*
import kotlin.Exception
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

    val chunkBlendWidth = 12
    val chunkBlendCornerRadius = 5

    // Build a chunk of the world, based on metadata.
    suspend fun carveWorldChunk() {
        val meta = App.save.getWorldMeta(x0, y0) ?: ChunkMeta()

        if (meta.biome == Ocean) {
            carveRoom(Rect(x0,y0,x1,y1), 0, TERRAIN_DEEP_WATER)
        } else {
            // Carve base terrain, blending biomes if needed
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
                forEachCell { x,y -> carve(x, y, 0, meta.biome.terrainAt(x,y)) }
            } else blendBiomes(meta, neighborMetas)

            // Add features
            if (meta.coasts.isNotEmpty()) buildCoasts(meta)
            if (meta.riverExits.isNotEmpty()) digRivers(meta)
            if (Dice.chance(0.05f) || forStarter) buildBuilding()
        }

        // Post-processing
        setRoofedInRock()
        setOverlaps()
        //addJunk(forAttract)
        //debugBorders()
    }

    private fun blendBiomes(meta: ChunkMeta, neighborMetas: MutableMap<XY,ChunkMeta?>) {
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
        // Biome-specific post-blend processing
        neighborMetas.forEach { dir, neighborMeta ->
            val bounds = when (dir) {
                NORTH -> Rect(0,0,CHUNK_SIZE-1,chunkBlendWidth-1)
                NORTHEAST -> Rect(CHUNK_SIZE-1-(chunkBlendWidth + chunkBlendCornerRadius), 0, CHUNK_SIZE-1,chunkBlendWidth-1)
                EAST -> Rect(CHUNK_SIZE-1-chunkBlendWidth,0, CHUNK_SIZE-1, CHUNK_SIZE-1)
                SOUTHEAST -> Rect(CHUNK_SIZE-1-(chunkBlendWidth + chunkBlendCornerRadius), CHUNK_SIZE-1-chunkBlendWidth, CHUNK_SIZE-1, CHUNK_SIZE-1)
                SOUTH -> Rect(0, CHUNK_SIZE-1-chunkBlendWidth, CHUNK_SIZE-1, CHUNK_SIZE-1)
                SOUTHWEST -> Rect(0,CHUNK_SIZE-1-chunkBlendWidth,chunkBlendWidth-1, CHUNK_SIZE-1)
                WEST -> Rect(0,0,chunkBlendWidth-1, CHUNK_SIZE-1)
                else -> Rect(0,0,chunkBlendWidth-1,chunkBlendWidth-1)
            }
            neighborMeta?.biome?.postBlendProcess(this, bounds)
        }
    }

    private fun buildCoasts(meta: ChunkMeta) {
        meta.coasts.forEach { edge ->
            if (edge in CARDINALS) {
                for (i in 0 until CHUNK_SIZE) {
                    when (edge) {
                        NORTH -> setTerrain(x0+i,y0, TERRAIN_DEEP_WATER)
                        SOUTH -> setTerrain(x0+i,y1, TERRAIN_DEEP_WATER)
                        WEST -> setTerrain(x0, y0+i, TERRAIN_DEEP_WATER)
                        EAST -> setTerrain(x1, y0+i, TERRAIN_DEEP_WATER)
                    }
                }
            } else {
                when (edge) {
                    NORTHWEST -> {
                        setTerrain(x0,y0, TERRAIN_DEEP_WATER)
                        setTerrain(x0+1,y0+1, TERRAIN_DEEP_WATER)
                    }
                    NORTHEAST -> {
                        setTerrain(x1,y0, TERRAIN_DEEP_WATER)
                        setTerrain(x1-1,y0+1, TERRAIN_DEEP_WATER)
                    }
                    SOUTHWEST -> {
                        setTerrain(x0,y1, TERRAIN_DEEP_WATER)
                        setTerrain(x0+1,y1-1, TERRAIN_DEEP_WATER)
                    }
                    SOUTHEAST -> {
                        setTerrain(x1,y1, TERRAIN_DEEP_WATER)
                        setTerrain(x1-1,y1-1, TERRAIN_DEEP_WATER)
                    }
                }
            }
        }
        repeat (4) { fuzzTerrain(TERRAIN_DEEP_WATER, 0.3f) }
        fringeTerrain(TERRAIN_DEEP_WATER, TERRAIN_SHALLOW_WATER, 1f)
        repeat (2) { fuzzTerrain(TERRAIN_SHALLOW_WATER, 0.4f) }
        fringeTerrain(TERRAIN_SHALLOW_WATER, TERRAIN_BEACH, 1f, TERRAIN_DEEP_WATER)
        repeat (2) { fuzzTerrain(TERRAIN_BEACH, 0.5f, TERRAIN_SHALLOW_WATER) }
    }

    private fun digRivers(meta: ChunkMeta) {
        when (meta.riverExits.size) {
            1 -> {
                val start = meta.riverExits[0]
                val endPos = XY(Dice.range(CHUNK_SIZE / 4, (CHUNK_SIZE / 4 * 3)), Dice.range(CHUNK_SIZE / 4, (CHUNK_SIZE / 4 * 3)))
                val end = RiverExit(pos = endPos, control = endPos, width = 1, edge = XY(0,0))
                drawRiver(start, end)
            }
            2 -> {
                drawRiver(meta.riverExits[0], meta.riverExits[1])
            }
            else -> {
                val variance = ((CHUNK_SIZE / 2) * 0.2f).toInt()
                val centerX = (CHUNK_SIZE / 2) + Dice.zeroTil(variance) - (variance / 2)
                val centerY = (CHUNK_SIZE / 2) + Dice.zeroTil(variance) - (variance / 2)
                val centerWidth = meta.riverExits.maxOf { it.width }
                val center = RiverExit(pos = XY(centerX, centerY), control = XY(centerX, centerY), width = centerWidth, edge = XY(0,0))
                meta.riverExits.forEach { exit ->
                    drawRiver(exit, center)
                }
            }
        }
        fuzzTerrain(GENERIC_WATER, meta.riverBlur * 0.4f)
        fringeTerrain(GENERIC_WATER, TERRAIN_GRASS, meta.riverGrass, TERRAIN_SHALLOW_WATER)
        fringeTerrain(GENERIC_WATER, TERRAIN_DIRT, meta.riverDirt, TERRAIN_SHALLOW_WATER)
        deepenWater()
    }

    private fun drawRiver(start: RiverExit, end: RiverExit) {
        var t = 0f
        var width = start.width.toFloat()
        val step = 0.02f
        val widthStep = (end.width - start.width) * step
        while (t < 1f) {
            val p = getBezier(t, start.pos.toXYf(), start.control.toXYf(), end.control.toXYf(), end.pos.toXYf())
            carveRoom(Rect((x0 + p.x - width/2).toInt(), (y0 + p.y - width/2).toInt(),
                (x0 + p.x + width/2).toInt(), (y0 + p.y + width/2).toInt()), 0, GENERIC_WATER, (width >= 3f))
            t += step
            width += widthStep
        }
    }

    private fun buildBuilding() {
        val facing = CARDINALS.random()
        carvePrefab(getPrefab(), Random.nextInt(x0, x1 - 20), Random.nextInt(y0, y1 - 20), facing)
        assignDoor(facing)
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
            if (getTerrain(x, y) == TERRAIN_PORTAL_DOOR) {

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
