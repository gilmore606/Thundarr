package world.gen.cartos

import App
import util.*
import world.*
import world.gen.NoisePatches
import world.gen.biomes.Beach
import world.gen.biomes.Biome
import world.gen.biomes.Blank
import world.gen.biomes.Ocean
import world.level.CHUNK_SIZE
import world.level.Level
import world.persist.LevelKeeper
import world.terrains.Terrain
import world.terrains.Terrain.Type.*
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

    val autoBridgeChance = 0.5f
    val autoBridgesInThisChunk = Dice.chance(autoBridgeChance)

    enum class CellFlag { NO_PLANTS, TRAIL, RIVER, RIVERBANK, OCEAN, BEACH }

    private val neighborMetas = mutableMapOf<XY,ChunkMeta?>()
    private val blendMap = Array(CHUNK_SIZE) { Array(CHUNK_SIZE) { mutableSetOf<Pair<Biome, Float>>() } }
    private val flagsMap = Array(CHUNK_SIZE) { Array(CHUNK_SIZE) { mutableSetOf<CellFlag>() } }

    private var hasBlends = false
    lateinit var meta: ChunkMeta
    private val riverIslandPoints = ArrayList<XY>()

    // Build a chunk of the world, based on metadata.
    suspend fun carveWorldChunk() {
        meta = App.save.getWorldMeta(x0, y0) ?: ChunkMeta()

        if (meta.biome == Ocean) {
            carveRoom(Rect(x0,y0,x1,y1), 0, TERRAIN_DEEP_WATER)
        } else {
            // Build blending map for neighbor biomes if needed
            buildBlendMap()

            // Carve base terrain, blending biomes if needed
            carveBaseTerrain()

            // Add features
            if (meta.coasts.isNotEmpty()) buildCoasts()
            if (meta.trailExits.isNotEmpty()) buildTrails()
            if (meta.hasLake) digLake()
            if (meta.riverExits.isNotEmpty()) digRivers()
            if (meta.roadExits.isNotEmpty()) buildRoads()

            // Carve extra terrain with no biome edge blending
            meta.biome.carveExtraTerrain(this)

            if (Dice.chance(0.05f) || forStarter) buildBuilding()
            repeat (meta.ruinedBuildings) { buildRandomRuin() }

            // Populate!
            growPlants()

            // Post-processing
            deepenWater()
            buildBridges()
            setRoofedInRock()
            setGeneratedBiomes()
            setOverlaps()
        }

        //debugBorders()
    }

    // Set per-cell biomes for things we generate like rivers, coastal beach, etc
    private fun setGeneratedBiomes() {
        forEachCell { x,y ->
            getTerrain(x,y).also { terrain ->
                when (terrain) {
                    TERRAIN_BEACH -> Beach
                    TERRAIN_SHALLOW_WATER -> Ocean
                    TERRAIN_STONEFLOOR -> Blank
                    else -> null
                }?.also { newBiome ->
                    blendMap[x-x0][y-y0].clear()
                    blendMap[x-x0][y-y0].add(Pair(newBiome,1f))
                }
            }
        }
    }

    private fun growPlants() {
        forEachBiome { x,y,biome ->
            if (!flagsMap[x-x0][y-y0].contains(CellFlag.NO_PLANTS) && Terrain.get(getTerrain(x,y)).canGrowPlants) {
                val fertility = NoisePatches.get("plantsBasic", x, y).toFloat()
                biome.addPlant(fertility, meta.variance, { addThing(x, y, it) }, { setTerrain(x, y, it) })
            }
        }
    }

    private suspend fun buildBlendMap() {
        neighborMetas.apply {
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
        if (hasBlends) {
            blendMap.forEach { it.forEach { it.add(Pair(meta.biome, 1f)) }}
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
        }
        // Roll each cell and set a specific biome by removing all other biome pairs from the blendmap
        forEachCell { x,y ->
            var biome: Biome? = null
            if (hasBlends) {
                val weights = blendMap[x - x0][y - y0]
                var weightTotal = 0f
                weights.forEach { weightTotal += it.second }
                var roll = Dice.float(0f, weightTotal)
                weights.forEach {
                    if (biome == null && roll <= it.second) {
                        biome = it.first
                    } else {
                        roll -= it.second
                    }
                }
            }
            blendMap[x - x0][y - y0].clear()
            blendMap[x - x0][y - y0].add(Pair(biome ?: meta.biome, 1f))
        }
        // Fill in orphaned single-cells in the blend with a neighbor
        forEachBiome { x, y, biome ->
            var switch: Biome? = null
            var orphan = true
            CARDINALS.from(x, y) { tx, ty, _ ->
                if (boundsCheck(tx, ty)) {
                    if (blendMap[tx-x0][ty-y0].first().first == biome) orphan = false
                    else switch = blendMap[tx-x0][ty-y0].first().first
                }
            }
            if (orphan) blendMap[x-x0][y-y0].apply {
                clear()
                add(Pair(switch!!, 1f))
            }
        }
    }

    private fun forEachBiome(doThis: (x: Int, y: Int, biome: Biome)->Unit) {
        forEachCell { x,y ->
            var biome = meta.biome
            blendMap[x-x0][y-y0].also { if (it.isNotEmpty()) biome = it.first().first }
            doThis(x, y, biome)
        }
    }

    private fun carveBaseTerrain() {
        forEachBiome { x,y,biome ->
            carve(x, y, 0, biome.terrainAt(x,y))
        }
        // Biome-specific post-blend processing
        if (hasBlends) {
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
    }

    private fun buildRoads() {
        fun buildRoadCell(x: Int, y: Int, width: Int, isVertical: Boolean) {
            repeat (width + 2) { n ->
                val lx = if (isVertical) x + n + x0 - (width / 2) else x + x0
                val ly = if (isVertical) y + y0 else y + n + y0 - (width / 2)
                if (n == 0 || n == width + 1) {
                    val current = getTerrain(lx, ly)
                    if ((NoisePatches.get("ruinWear", lx, ly) < 0.001f) && current != TERRAIN_HIGHWAY_H && current != TERRAIN_HIGHWAY_V
                        && current != GENERIC_WATER && current != TERRAIN_SHALLOW_WATER && current != TERRAIN_DEEP_WATER) {
                        setTerrain(lx, ly, TERRAIN_DIRT)
                    }
                } else {
                    val t = if (isVertical) TERRAIN_HIGHWAY_V else TERRAIN_HIGHWAY_H
                    setRuinTerrain(lx, ly, 0.34f, t)
                }
            }
        }
        val mid = CHUNK_SIZE/2
        val end = CHUNK_SIZE-1
        meta.roadExits.forEach { exit ->
            when (exit.edge) {
                NORTH -> drawLine(XY(mid, 0), XY(mid, mid)) { x,y -> buildRoadCell(x,y,exit.width,true) }
                SOUTH -> drawLine(XY(mid, mid), XY(mid,end)) { x,y -> buildRoadCell(x,y,exit.width,true) }
                WEST -> drawLine(XY(0,mid+1), XY(mid, mid+1)) { x,y -> buildRoadCell(x,y,exit.width,false) }
                EAST -> drawLine(XY(mid, mid+1), XY(end, mid+1)) { x,y -> buildRoadCell(x,y,exit.width,false) }
            }
        }
    }

    private fun buildRandomRuin() {
        val mid = CHUNK_SIZE/2
        if (meta.roadExits.isNotEmpty()) {
            val offset = Dice.range(3, 8) * Dice.sign()
            val along = Dice.range(2, CHUNK_SIZE/2-2)
            val width = Dice.range(4, 10)
            val height = Dice.range(4, 10)
            val ruinEdge = meta.roadExits.random().edge
            when (ruinEdge) {
                NORTH -> buildRuin(mid + offset + width * (if (offset<0) -1 else 0), along, width, height)
                SOUTH -> buildRuin(mid + offset + width * (if (offset<0) -1 else 0), CHUNK_SIZE-along, width, height)
                WEST -> buildRuin(along, mid + offset + width * (if (offset<0) -1 else 0), width, height)
                EAST -> buildRuin(CHUNK_SIZE-along, mid + offset + width * (if (offset<0) -1 else 0), width, height)
            }
        } else {
            buildRuin(Dice.range(1, CHUNK_SIZE-10), Dice.range(1, CHUNK_SIZE-10), Dice.range(4, 10), Dice.range(4, 10))
        }
    }

    private fun buildRuin(x: Int, y: Int, width: Int, height: Int) {
        for (ix in x until x + width) {
            for (iy in y until y + height) {
                if (ix == x || ix == x + width - 1 || iy == y || iy == y + height - 1) {
                    setRuinTerrain(ix + x0, iy + y0, 0.34f,
                        if (Dice.chance(0.9f))
                            TERRAIN_BRICKWALL else null)
                } else {
                    setRuinTerrain(ix + x0, iy + y0, 0.34f,
                        if (Dice.chance(NoisePatches.get("ruinWear", ix + x0, iy + y0).toFloat()))
                            null else TERRAIN_STONEFLOOR)
                }
            }
        }
    }

    private fun setRuinTerrain(x: Int, y: Int, wear: Float, terrain: Terrain.Type?) {
        if (terrain == null) return
        if (NoisePatches.get("ruinWear", x, y) < wear) {
            if (boundsCheck(x, y)) {
                setTerrain(x, y, terrain)
            }
        }
    }

    private fun buildCoasts() {
        val cornerWater = growOblong(8, 8)
        meta.coasts.forEach { edge ->
            if (edge in CARDINALS) {
                for (i in 0 until CHUNK_SIZE) {
                    when (edge) {
                        NORTH -> setTerrain(x0+i,y0, GENERIC_WATER)
                        SOUTH -> setTerrain(x0+i,y1, GENERIC_WATER)
                        WEST -> setTerrain(x0, y0+i, GENERIC_WATER)
                        EAST -> setTerrain(x1, y0+i, GENERIC_WATER)
                    }
                }
            } else {
                when (edge) {
                    NORTHWEST -> {
                        printGrid(cornerWater, x0, y0, GENERIC_WATER)
                        setTerrain(x0,y0, GENERIC_WATER)
                        setTerrain(x0+1,y0+1, GENERIC_WATER)
                    }
                    NORTHEAST -> {
                        printGrid(cornerWater, x1-7, y0, GENERIC_WATER)
                        setTerrain(x1,y0, GENERIC_WATER)
                        setTerrain(x1-1,y0+1, GENERIC_WATER)
                    }
                    SOUTHWEST -> {
                        printGrid(cornerWater, x0, y1-7, GENERIC_WATER)
                        setTerrain(x0,y1, GENERIC_WATER)
                        setTerrain(x0+1,y1-1, GENERIC_WATER)
                    }
                    SOUTHEAST -> {
                        printGrid(cornerWater, x1-7, y1-7, GENERIC_WATER)
                        setTerrain(x1,y1, GENERIC_WATER)
                        setTerrain(x1-1,y1-1, GENERIC_WATER)
                    }
                }
            }
        }
        repeat (8) { fuzzTerrain(GENERIC_WATER, 0.3f) }
        fringeTerrain(GENERIC_WATER, TERRAIN_BEACH, 1f)
        repeat ((2 + 3 * meta.variance).toInt()) { fuzzTerrain(TERRAIN_BEACH, meta.variance, GENERIC_WATER) }
        forEachCell { x,y ->
            if (getTerrain(x,y) == GENERIC_WATER) {
                flagsMap[x-x0][y-y0].add(CellFlag.OCEAN)
                flagsMap[x-x0][y-y0].add(CellFlag.NO_PLANTS)
            } else if (getTerrain(x,y) == TERRAIN_BEACH) {
                flagsMap[x-x0][y-y0].add(CellFlag.BEACH)
                flagsMap[x-x0][y-y0].add(CellFlag.NO_PLANTS)
            }
        }
    }

    private fun buildTrails() {
        when (meta.trailExits.size) {
            1 -> {
                val start = meta.trailExits[0]
                val endPos = XY(Dice.range(CHUNK_SIZE / 4, (CHUNK_SIZE / 4 * 3)), Dice.range(CHUNK_SIZE / 4, (CHUNK_SIZE / 4 * 3)))
                val end = TrailExit(pos = endPos, control = endPos, edge = XY(0,0))
                drawTrail(start, end)
            }
            2 -> {
                drawTrail(meta.trailExits[0], meta.trailExits[1])
            }
            else -> {
                val variance = ((CHUNK_SIZE / 2) * 0.2f).toInt()
                val centerX = (CHUNK_SIZE / 2) + Dice.zeroTil(variance) - (variance / 2)
                val centerY = (CHUNK_SIZE / 2) + Dice.zeroTil(variance) - (variance / 2)
                val center = TrailExit(pos = XY(centerX, centerY), control = XY(centerX, centerY), edge = XY(0,0))
                meta.trailExits.forEach { exit ->
                    drawTrail(exit, center)
                }
            }
        }
    }

    private fun drawTrail(start: TrailExit, end: TrailExit) {
        var t = 0f
        val step = 0.02f
        while (t < 1f) {
            val p = getBezier(t, start.pos.toXYf(), start.control.toXYf(), end.control.toXYf(), end.pos.toXYf())
            carveTrailChunk(Rect((x0 + p.x).toInt(), (y0 + p.y).toInt(),
                (x0 + p.x + 1).toInt(), (y0 + p.y + 1).toInt()), meta.biome.trailTerrain((x0 + p.x).toInt(), (y0 + p.y).toInt()), false)
            t += step
        }
    }

    private fun carveTrailChunk(room: Rect,
                            type: Terrain.Type = Terrain.Type.TERRAIN_STONEFLOOR,
                            skipCorners: Boolean = false, skipTerrain: Terrain.Type? = null) {
        for (x in room.x0..room.x1) {
            for (y in room.y0..room.y1) {
                if (x >= x0 && y >= y0 && x <= x1 && y <= y1) {
                    if (!skipCorners || !((x == x0 || x == x1) && (y == y0 || y == y1))) {
                        if (!flagsMap[x-x0][y-y0].contains(CellFlag.OCEAN) && !flagsMap[x-x0][y-y0].contains(CellFlag.BEACH)) {
                            if (skipTerrain == null || getTerrain(x, y) != skipTerrain) {
                                setTerrain(x, y, type)
                                flagsMap[x - x0][y - y0].add(CellFlag.TRAIL)
                                flagsMap[x - x0][y - y0].add(CellFlag.NO_PLANTS)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun digRivers() {
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
        if (riverIslandPoints.isNotEmpty() && Dice.chance(0.5f)) {
            riverIslandPoints.random().also { island ->
                printGrid(growBlob(Dice.range(3,6), Dice.range(3,6)), island.x, island.y, meta.biome.baseTerrain)
            }
        }
        fuzzTerrain(GENERIC_WATER, meta.riverBlur * 0.4f)
        addRiverBanks()
    }

    private fun addRiverBanks() {
        forEachBiome { x, y, biome ->
            if (getTerrain(x,y) == Terrain.Type.GENERIC_WATER) {
                DIRECTIONS.from(x, y) { dx, dy, _ ->
                    if (boundsCheck(dx, dy) && getTerrain(dx, dy) != Terrain.Type.GENERIC_WATER) {
                        if (getTerrain(dx, dy) != Terrain.Type.TERRAIN_BEACH) {
                            flagsMap[dx - x0][dy - y0].add(CellFlag.RIVERBANK)
                        }
                    }
                }
            }
        }
        repeat (3) {
            val adds = ArrayList<XY>()
            forEachBiome { x, y, biome ->
                if (getTerrain(x, y) != GENERIC_WATER && !flagsMap[x - x0][y - y0].contains(CellFlag.RIVERBANK) &&
                        !flagsMap[x-x0][y-y0].contains(CellFlag.TRAIL)) {
                    var n = 0
                    DIRECTIONS.from(x, y) { dx, dy, dir ->
                        if (boundsCheck(dx,dy) && flagsMap[dx - x0][dy - y0].contains(CellFlag.RIVERBANK)) n++
                    }
                    val v = (NoisePatches.get("ruinMicro", x, y) * 3f).toInt()
                    if (n > 1 && Dice.chance(n * 0.15f + v * 0.4f)) adds.add(XY(x,y))
                }
            }
            adds.forEach { flagsMap[it.x - x0][it.y - y0].add(CellFlag.RIVERBANK) }
        }
        forEachBiome { x,y,biome ->
            if (flagsMap[x-x0][y-y0].contains(CellFlag.RIVERBANK)) {
                setTerrain(x,y,biome.riverBankTerrain(x,y))
            }
        }
    }

    private fun drawRiver(start: RiverExit, end: RiverExit) {
        val startWidth = if (start.edge in meta.coasts) start.width * 2f + 3f else start.width.toFloat()
        val endWidth = if (end.edge in meta.coasts) end.width * 2f + 3f else end.width.toFloat()
        var t = 0f
        var width = startWidth
        val step = 0.02f
        val widthStep = (endWidth - startWidth) * step
        while (t < 1f) {
            val p = getBezier(t, start.pos.toXYf(), start.control.toXYf(), end.control.toXYf(), end.pos.toXYf())
            carveRiverChunk(Rect((x0 + p.x - width/2).toInt(), (y0 + p.y - width/2).toInt(),
                (x0 + p.x + width/2).toInt(), (y0 + p.y + width/2).toInt()), (width >= 3f))
            if (t > 0.2f && t < 0.8f && width > 6 && Dice.chance(0.1f)) {
                riverIslandPoints.add(XY((x0 + p.x).toInt(), (y0 + p.y).toInt()))
            }
            t += step
            width += widthStep
        }
    }

    private fun carveRiverChunk(room: Rect, skipCorners: Boolean) {
        for (x in room.x0..room.x1) {
            for (y in room.y0..room.y1) {
                if (x >= x0 && y >= y0 && x <= x1 && y <= y1) {
                    if (!skipCorners || !((x == x0 || x == x1) && (y == y0 || y == y1))) {
                        setTerrain(x, y, GENERIC_WATER)
                        flagsMap[x-x0][y-y0].add(CellFlag.RIVER)
                    }
                }
            }
        }
    }

    private fun buildBridges() {
        if (autoBridgesInThisChunk) {
            var bridgeCellsLeft = Dice.range(4, 16)
            forEachCell { x, y ->
                if (flagsMap[x - x0][y - y0].contains(CellFlag.TRAIL) && flagsMap[x - x0][y - y0].contains(CellFlag.RIVER)) {
                    if (bridgeCellsLeft > 0) {
                        setTerrain(x, y, TERRAIN_PAVEMENT)
                        bridgeCellsLeft--
                    }
                }
            }
        }
    }

    private fun digLake() {
        if (Dice.chance(0.15f)) {
            // Big lake!
            val width = Dice.range(40,56)
            val height = Dice.range(40,56)
            digLakeBlobAt(Dice.range(3, 6), Dice.range(3,6), width, height)
            if (Dice.chance(0.5f)) {
                // Big lake island
                printGrid(growBlob(Dice.range(4,12), Dice.range(4,12)), Dice.range(10, 45), Dice.range(10, 45), meta.biome.baseTerrain)
            }
            return
        }
        val width = Dice.range(12,31)
        val height = Dice.range(12,31)
        val x = x0 + Dice.range(width, CHUNK_SIZE - width) - width / 2
        val y = y0 + Dice.range(height, CHUNK_SIZE - height) - height / 2
        digLakeBlobAt(x, y, width, height)
        if (Dice.chance(0.4f)) {
            // Double lake
            val ox = x + Dice.range(-(width / 2), width / 2)
            val oy = y + Dice.range(-(height / 2), height / 2)
            val owidth = (width * Dice.float(0.3f,0.7f)).toInt()
            val oheight = (height * Dice.float(0.3f, 0.7f)).toInt()
            digLakeBlobAt(ox, oy, owidth, oheight)
        }
        if (width > 17 && height > 17 && Dice.chance(0.5f)) {
            // Island
            printGrid(growBlob((width * Dice.float(0.1f, 0.7f)).toInt(), (height * Dice.float(0.1f, 0.7f)).toInt()),
            x + Dice.range(3,15), y + Dice.range(3, 15), meta.biome.baseTerrain)
        }
    }

    private fun digLakeBlobAt(x: Int, y: Int, width: Int, height: Int) {
        printGrid(growBlob(width, height), x, y, GENERIC_WATER)
    }

    private fun buildBuilding() {
        val facing = CARDINALS.random()
        carvePrefab(getPrefab(), Random.nextInt(x0, x1 - 20), Random.nextInt(y0, y1 - 20), facing)
        assignDoor(facing)
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
