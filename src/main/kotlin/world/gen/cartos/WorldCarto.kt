package world.gen.cartos

import App
import audio.Speaker
import things.Bonepile
import things.Glowstone
import things.Trunk
import util.*
import world.*
import world.gen.NoisePatches
import world.gen.biomes.Beach
import world.gen.biomes.Biome
import world.gen.biomes.Blank
import world.gen.biomes.Ocean
import world.level.CHUNK_SIZE
import world.level.Level
import world.terrains.Terrain
import world.terrains.Terrain.Type.*
import java.lang.Float.max
import java.lang.Float.min
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

    val globalPlantDensity = 0.2f

    val cavePortalChance = 0.5f
    val ruinTreasureChance = 0.4f

    enum class CellFlag { NO_PLANTS, NO_BUILDINGS, TRAIL, RIVER, RIVERBANK, OCEAN, BEACH }

    private val neighborMetas = mutableMapOf<XY,ChunkMeta?>()
    private val blendMap = Array(CHUNK_SIZE) { Array(CHUNK_SIZE) { mutableSetOf<Pair<Biome, Float>>() } }
    private val flagsMap = Array(CHUNK_SIZE) { Array(CHUNK_SIZE) { mutableSetOf<CellFlag>() } }
    private val fertMap = Array(CHUNK_SIZE) { Array<Float?>(CHUNK_SIZE) { null } }

    private var hasBlends = false
    lateinit var meta: ChunkMeta
    private val riverIslandPoints = ArrayList<XY>()
    private val cavePortalPoints = ArrayList<XY>()

    // Build a chunk of the world, based on metadata.
    suspend fun carveWorldChunk() {
        meta = App.save.getWorldMeta(x0, y0) ?: throw RuntimeException("No meta found for chunk $x0 $y0 !")

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

            if (Dice.chance(0.01f) || forStarter) buildStructureDungeon()
            repeat (meta.ruinedBuildings) { buildRandomRuin() }
            if (meta.lavaExits.isNotEmpty()) buildLava()
            if (meta.hasVolcano) buildVolcano()

            digCave()

            // Populate!
            buildFertilityMap()
            growPlants()

            // Post-processing
            deepenWater()
            pruneTrees()
            buildBridges()
            setRoofedInRock()
            setGeneratedBiomes()
            setOverlaps()
        }

        //debugBorders()
    }

    private fun pruneTrees() {
        forEachCell { x,y ->
            if (y in (y0 + 1) until y1) {
                val type = getTerrain(x, y)
                if (Terrain.get(type).pruneVerticalOrphans()) {
                    val upper = getTerrain(x, y - 1)
                    val lower = getTerrain(x, y + 1)
                    if (upper != type && lower != type) {
                        setTerrain(x, y, if (Dice.flip()) upper else lower)
                    }
                }
            }
        }
    }

    private fun digCave() {
        val entrances = mutableSetOf<XY>()
        forEachTerrain(TERRAIN_CAVEWALL) { x,y ->
            val free = neighborCount(x, y, CARDINALS) { dx,dy -> isWalkableAt(dx,dy) }
            if (free == 1) entrances.add(XY(x,y))
        }
        if (entrances.isNotEmpty()) {
            cavePortalPoints.clear()
            var cellCount = 0
            repeat (kotlin.math.min(entrances.size, Dice.oneTo(4))) {
                val entrance = entrances.random()
                cellCount += recurseCave(entrance.x, entrance.y, 1f, Dice.float(0.02f, 0.12f))
                chunk.setRoofed(entrance.x, entrance.y, Chunk.Roofed.WINDOW)
            }
            if (cellCount > 6) {
                val usablePoints = cavePortalPoints.filter { point ->
                    CARDINALS.hasOneWhere { !isWalkableAt(it.x + point.x, it.y + point.y) }
                }.toMutableList()
                if (usablePoints.isNotEmpty() && Dice.chance(cavePortalChance)) {
                    val caveEntrance = usablePoints.random()
                    usablePoints.remove(caveEntrance)
                    buildCaveDungeon(caveEntrance)
                    if (usablePoints.isNotEmpty()) {
                        val lightPos = usablePoints.random()
                        val light = Glowstone().withColor(0.1f, 0.2f, 0.5f)
                        addThing(lightPos.x, lightPos.y, light)
                    }
                }
            }
        }
    }

    private fun recurseCave(x: Int, y: Int, density: Float, falloff: Float): Int {
        setTerrain(x, y, TERRAIN_CAVEFLOOR)
        chunk.setRoofed(x, y, Chunk.Roofed.INDOOR)
        var continuing = false
        var count = 1
        CARDINALS.from(x, y) { dx, dy, _ ->
            if (boundsCheck(dx, dy) && getTerrain(dx, dy) == TERRAIN_CAVEWALL) {
                var ok = true
                DIRECTIONS.from(dx, dy) { ddx, ddy, _ ->
                    if (boundsCheck(ddx,ddy)) {
                        val testTerrain = getTerrain(ddx,ddy)
                        if (testTerrain != TERRAIN_CAVEFLOOR && testTerrain != TERRAIN_CAVEWALL) ok = false
                    }
                }
                if (ok && Dice.chance(density)) {
                    continuing = true
                    count += recurseCave(dx, dy, density - falloff, falloff)
                }
            }
        }
        if (!continuing) cavePortalPoints.add(XY(x, y))
        return count
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

    private fun buildFertilityMap() {
        forEachBiome { x,y,biome ->
            val terrain = Terrain.get(getTerrain(x,y))
            if (!flagsMap[x-x0][y-y0].contains(CellFlag.NO_PLANTS) && terrain.canGrowPlants) {
                var fert = biome.fertilityAt(x, y) + terrain.fertilityBonus()
                val nearTrees = neighborCount(x, y, DIRECTIONS) { x,y -> getTerrain(x,y) == TERRAIN_TEMPERATE_FORESTWALL } > 0
                val nearWater = neighborCount(x, y, DIRECTIONS) { x,y -> getTerrain(x,y) == GENERIC_WATER } > 0
                if (nearTrees) fert += 0.4f
                if (nearWater) fert += 0.4f
                if (Dice.chance(0.2f)) fert += Dice.float(-0.3f, 0.3f)
                fertMap[x-x0][y-y0] = max(0.0f, min(1.0f, fert))
            }
        }
    }

    private fun growPlants() {
        forEachBiome { x,y,biome ->
            fertMap[x-x0][y-y0]?.also { fertility ->
                getPlant(biome, meta.habitat, fertility, globalPlantDensity)?.also {
                    addThing(x, y, it)
                }
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
                    else switch = blendMap[tx-x0][ty-y0].first().first // TODO: make this random!
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

    private fun biomeAt(x: Int, y: Int) = if (boundsCheck(x, y)) {
        blendMap[x-x0][y-y0].let { if (it.isNotEmpty()) it.first().first else meta.biome }
    } else meta.biome

    private fun carveBaseTerrain() {
        forEachBiome { x,y,biome ->
            var t = biome.terrainAt(x,y)
            if (t == TERRAIN_TEMPERATE_FORESTWALL) t = meta.habitat.forestWallType()
            carve(x, y, 0, t)
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
                val wear = NoisePatches.get("ruinWear", lx, ly).toFloat()
                val current = getTerrain(lx, ly)
                flagsMap[lx-x0][ly-y0].add(CellFlag.NO_BUILDINGS)
                if (n == 0 || n == width + 1) {
                    if (current != TERRAIN_HIGHWAY_H && current != TERRAIN_HIGHWAY_V && current != GENERIC_WATER) {
                        if (wear < 0.001f || Dice.chance(1f - wear)) {
                            setTerrain(lx, ly, biomeAt(lx, ly).trailTerrain(lx, ly))
                        }
                    }
                } else {
                    val t = if (isVertical) TERRAIN_HIGHWAY_V else TERRAIN_HIGHWAY_H
                    if (wear < 0.34f || (current != GENERIC_WATER && Dice.chance(0.7f - wear))) {
                        setTerrain(lx, ly, t)
                    } else if (current != GENERIC_WATER) {
                        setTerrain(lx, ly, biomeAt(lx, ly).trailTerrain(lx, ly))
                        flagsMap[lx-x0][ly-y0].add(CellFlag.NO_PLANTS)
                    }
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
        for (ix in x until x+width) {
            for (iy in y until y+height) {
                if (ix>=0 && iy>=0 && ix<CHUNK_SIZE && iy<CHUNK_SIZE && flagsMap[ix][iy].contains(CellFlag.NO_BUILDINGS)) return
            }
        }
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
        if (Dice.chance(ruinTreasureChance)) {
            var placed = false
            while (!placed) {
                val tx = Dice.range(x-2, x+2) + x0
                val ty = Dice.range(y-2, y+2) + y0
                if (boundsCheck(tx,ty) && isWalkableAt(tx,ty)) {
                    val treasure = if (Dice.chance(0.25f)) Trunk() else Bonepile()
                    addThing(tx, ty, treasure)
                    placed = true
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
                if (neighborCount(x-x0,y-y0, GENERIC_WATER) < 8) chunk.setSound(x,y, Speaker.PointAmbience(Speaker.Ambience.OCEAN))
            } else if (getTerrain(x,y) == TERRAIN_BEACH) {
                flagsMap[x-x0][y-y0].add(CellFlag.BEACH)
                flagsMap[x-x0][y-y0].add(CellFlag.NO_PLANTS)
            }
        }
    }

    private fun buildLava() {
        var bridgeDir: XY? = null
        when (meta.lavaExits.size) {
            1 -> {
                val start = meta.lavaExits[0]
                val endPos = XY(Dice.range(CHUNK_SIZE / 4, (CHUNK_SIZE / 4 * 3)), Dice.range(CHUNK_SIZE / 4, (CHUNK_SIZE / 4 * 3)))
                val end = LavaExit(pos = endPos, edge = XY(0,0), width = 1)
                drawLava(start, end)
            }
            2 -> {
                val e1 = meta.lavaExits[0]
                val e2 = meta.lavaExits[1]
                drawLava(e1, e2)
                if (e1.width <= 6) {
                    if ((e1.edge == NORTH && e2.edge == SOUTH) || (e1.edge == SOUTH && e2.edge == NORTH)) {
                        bridgeDir = EAST
                    } else if ((e1.edge == EAST && e2.edge == WEST) || (e1.edge == WEST && e2.edge == EAST)) {
                        bridgeDir = NORTH
                    }
                }
            }
            else -> {
                val variance = ((CHUNK_SIZE / 2) * 0.2f).toInt()
                val centerX = (CHUNK_SIZE / 2) + Dice.zeroTil(variance) - (variance / 2)
                val centerY = (CHUNK_SIZE / 2) + Dice.zeroTil(variance) - (variance / 2)
                val center = LavaExit(pos = XY(centerX, centerY), edge = XY(0,0), width = meta.lavaExits[0].width + 1)
                meta.lavaExits.forEach { exit ->
                    drawLava(exit, center)
                }
            }
        }
        fuzzTerrain(TERRAIN_LAVA, 0.5f)
        bridgeDir?.also { addLavaBridge(it) }
    }

    private fun drawLava(start: LavaExit, end: LavaExit) {
        var t = 0f
        val step = 0.02f
        var width = start.width.toFloat()
        val widthStep = (end.width.toFloat() - start.width.toFloat()) * step
        while (t < 1f) {
            val p = getBezier(t, start.pos.toXYf(), start.pos.toXYf(), end.pos.toXYf(), end.pos.toXYf())
            val px = x0 + p.x
            val py = y0 + p.y
            carveTrailChunk(Rect((px - width/2).toInt(), (py - width/2).toInt(),
                (px + width/2).toInt(), (py + width/2).toInt()), TERRAIN_LAVA, false)
            val edgeWidth = width + 2.5f + width * (NoisePatches.get("metaVariance",px.toInt(),py.toInt()).toFloat()) * 1.5f
            carveTrailChunk(Rect((px - edgeWidth / 2).toInt(), (py - edgeWidth / 2).toInt(),
                (px + edgeWidth/2).toInt(), (py + edgeWidth/2).toInt()), TERRAIN_ROCKS, true, TERRAIN_LAVA)
            t += step
            width += widthStep
        }
    }

    private fun addLavaBridge(dir: XY) {
        val cross = (CHUNK_SIZE / 4) + Dice.zeroTo(CHUNK_SIZE-30)
        for (i in 0 until CHUNK_SIZE) {
            val x = x0 + if (dir == NORTH) cross else i
            val y = y0 + if (dir == NORTH) i else cross
            if (getTerrain(x,y) == TERRAIN_LAVA) {
                setTerrain(x,y,TERRAIN_ROCKS)
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
            val terrain = biomeAt(x0 + p.x.toInt(), y0 + p.y.toInt()).trailTerrain(x0 + p.x.toInt(), y0 + p.y.toInt())
            carveTrailChunk(Rect((x0 + p.x).toInt(), (y0 + p.y).toInt(),
                (x0 + p.x + 1).toInt(), (y0 + p.y + 1).toInt()), terrain, false)
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
                                flagsMap[x - x0][y - y0].add(CellFlag.NO_BUILDINGS)
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
            chunk.setSound(x0 + p.x.toInt(), y0 + p.y.toInt(),
                if (width < 4) Speaker.PointAmbience(Speaker.Ambience.RIVER1, 30f, 1f)
                else if (width < 8) Speaker.PointAmbience(Speaker.Ambience.RIVER2, 40f, 1f)
                else Speaker.PointAmbience(Speaker.Ambience.RIVER3, 50f, 1f))
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

    private fun buildVolcano() {
        val width = Dice.range(40,56)
        val height = Dice.range(40,56)
        printGrid(growBlob(width, height), x0 + Dice.range(3,6), y0 + Dice.range(3,6), TERRAIN_LAVA)
        fringeTerrain(TERRAIN_LAVA, TERRAIN_ROCKS, 0.7f)
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
        fringeTerrain(TEMP1, TEMP2, 1f)
        repeat (3) { varianceFuzzTerrain(TEMP2, TEMP1) }
        swapTerrain(TEMP1, GENERIC_WATER)
        forEachCell { x,y ->
            if (getTerrain(x,y) == TEMP2) {
                setTerrain(x,y,meta.biome.riverBankTerrain(x,y))
            }
        }
    }

    private fun digLakeBlobAt(x: Int, y: Int, width: Int, height: Int) {
        printGrid(growBlob(width, height), x, y, TEMP1)
    }

    private fun buildStructureDungeon() {
        val facing = CARDINALS.random()
        carvePrefab(getPrefab(), Random.nextInt(x0, x1 - 20), Random.nextInt(y0, y1 - 20), facing)
        if (forStarter) {
            log.info("Looking for door for starter dungeon...")
        } else {
            log.info("Building structure dungeon...")
        }
        forEachCell { x, y ->
            if (getTerrain(x, y) == TERRAIN_PORTAL_DOOR) {
                val building = if (forStarter)
                    StarterDungeon().at(x,y).facing(facing)
                else
                    BoringBuilding().at(x,y).facing(facing)
                connectBuilding(building)
            }
        }
    }

    private fun buildCaveDungeon(doorPos: XY) {
        log.info("Building cave dungeon...")
        setTerrain(doorPos.x, doorPos.y, TERRAIN_PORTAL_CAVE)
        val facings = mutableListOf<XY>()
        CARDINALS.from(doorPos.x, doorPos.y) { dx, dy, dir ->
            if (boundsCheck(dx,dy) && isWalkableAt(dx,dy)) facings.add(dir)
        }
        connectBuilding(NaturalCavern().at(doorPos.x, doorPos.y).facing(facings.random()))
    }

}
