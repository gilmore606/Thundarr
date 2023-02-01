package world.gen.cartos

import com.badlogic.gdx.Gdx
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import things.LitThing
import kotlin.random.Random
import things.Thing
import util.*
import world.Building
import world.Chunk
import world.gen.NoisePatches
import world.gen.biomes.Biome
import world.gen.habitats.Habitat
import world.gen.plantSpawns
import world.level.Level
import world.gen.prefabs.Prefab
import world.gen.prefabs.TiledFile
import world.level.CHUNK_SIZE
import world.persist.LevelKeeper
import world.terrains.Floor
import world.terrains.Terrain
import world.terrains.TerrainData

abstract class Carto(
    val x0: Int,
    val y0: Int,
    val x1: Int,
    val y1: Int,
    val chunk: Chunk,
    val level: Level
) {

    protected var regions: Array<Array<Int?>> = Array(1+x1-x0) { Array(1+y1-y0) { null } }

    protected var innerBounds = Rect(x0+1,y0+1,x1-1,y1-1)
    protected val width = x1 - x0
    protected val height = y1 - y0

    protected val json = Json { ignoreUnknownKeys = true }

    protected val plantSpawns = plantSpawns()

    init {
        for (x in x0 .. x1) {
            for (y in y0 .. y1) {
                chunk.setTerrain(x, y, Terrain.Type.TERRAIN_BRICKWALL)
            }
        }
    }

    fun getTerrain(x: Int, y: Int) = chunk.getTerrain(x,y)
    fun setTerrain(x: Int, y: Int, type: Terrain.Type) = chunk.setTerrain(x, y, type, Terrain.get(type).isOpaque())
    protected fun setTerrainData(x: Int, y: Int, data: TerrainData) = chunk.setTerrainData(x, y, data)
    protected fun isWalkableAt(x: Int, y: Int) = chunk.isWalkableAt(x, y)

    fun addThing(x: Int, y: Int, thing: Thing) {
        val dest = chunk.cellContainerAt(x, y)
        dest?.reconnect(level, x, y)
        thing.moveTo(dest)
        if (thing is LitThing && thing.active) {
            thing.reproject()
        }
    }

    protected fun regionAt(x: Int, y: Int): Int? = try {
        regions[x - x0][y - y0]
    } catch (e: ArrayIndexOutOfBoundsException) { null }
    protected fun regionAt(xy: XY): Int? = regionAt(xy.x, xy.y)

    protected fun setRegionAt(x: Int, y: Int, region: Int) = try {
        regions[x - x0][y - y0] = region
    } catch (_: ArrayIndexOutOfBoundsException) { }

    // Can the given tile be carved into?
    protected fun isRock(x: Int, y: Int): Boolean {
        return !Terrain.get(chunk.getTerrain(x, y)).isWalkable()
    }
    protected fun isRock(xy: XY): Boolean = isRock(xy.x, xy.y)

    protected fun randomRect(
        extraSize: Int
    ): Rect {
        val size = Random.nextInt(1, 3 + extraSize) * 2 + 1
        val rectangularity = Random.nextInt(0, 1 + size / 2) * 2
        var width = size
        var height = size
        if (Random.nextFloat() > 0.5f) {
            width += rectangularity
        } else {
            height += rectangularity
        }
        val x = Random.nextInt((this.width - width) / 2) * 2 + 1 + x0
        val y = Random.nextInt((this.height - height) / 2) * 2 + 1 + y0
        return Rect(x, y, x + width - 1, y + height - 1)
    }

    // TODO: don't pass regionId here, compute it automatically in a stage
    // where we floodfill successively until all space is regions
    protected fun carve(x: Int, y: Int, regionId: Int,
                        type: Terrain.Type = Terrain.Type.TERRAIN_STONEFLOOR) {
        setTerrain(x, y, type)
        setRegionAt(x, y, regionId)
    }
    protected fun carve(xy: XY, regionId: Int,
                        type: Terrain.Type = Terrain.Type.TERRAIN_STONEFLOOR) {
        carve(xy.x, xy.y, regionId, type)
    }

    protected fun carveRoom(room: Rect, regionId: Int,
                            type: Terrain.Type = Terrain.Type.TERRAIN_STONEFLOOR,
                            skipCorners: Boolean = false, skipTerrain: Terrain.Type? = null) {
        for (x in room.x0..room.x1) {
            for (y in room.y0..room.y1) {
                if (x >= x0 && y >= y0 && x <= x1 && y <= y1) {
                    if (!skipCorners || !((x == x0 || x == x1) && (y == y0 || y == y1))) {
                        if (skipTerrain == null || getTerrain(x, y) != skipTerrain) {
                            setTerrain(x, y, type)
                            setRegionAt(x, y, regionId)
                        }
                    }
                }
            }
        }
    }

    protected fun carvePrefab(prefab: Prefab, atX: Int, atY: Int, facing: XY) {
        for (nx in 0 until prefab.width) {
            for (ny in 0 until prefab.height) {
                val x = when (facing) {
                    NORTH -> nx
                    SOUTH -> prefab.width - nx - 1
                    WEST -> ny
                    EAST -> prefab.height - ny - 1
                    else -> 0
                }
                val y = when (facing) {
                    NORTH -> ny
                    SOUTH -> prefab.height - ny - 1
                    WEST -> nx
                    EAST -> prefab.width - nx - 1
                    else -> 0
                }
                prefab.terrain[nx][ny]?.also { type ->
                    setTerrain(x + atX, y + atY, type)
                }
            }
        }
    }

    protected fun forEachCell(doThis: (x: Int, y: Int) -> Unit) {
        for (x in x0..x1) {
            for (y in y0..y1) {
                doThis(x,y)
            }
        }
    }

    fun neighborCount(x: Int, y: Int, type: Terrain.Type): Int {
        var c = 0
        if (getTerrain(x-1,y) == type) c++
        if (getTerrain(x+1,y) == type) c++
        if (getTerrain(x-1,y-1) == type) c++
        if (getTerrain(x,y-1) == type) c++
        if (getTerrain(x+1,y-1) == type) c++
        if (getTerrain(x-1,y+1) == type) c++
        if (getTerrain(x,y+1) == type) c++
        if (getTerrain(x+1,y+1) == type) c++
        return c
    }

    fun neighborCount(x: Int, y: Int, match: (Terrain.Type)->Boolean): Int {
        var c = 0
        if (match(getTerrain(x-1,y))) c++
        if (match(getTerrain(x+1,y))) c++
        if (match(getTerrain(x,y-1))) c++
        if (match(getTerrain(x,y+1))) c++
        if (match(getTerrain(x-1,y-1))) c++
        if (match(getTerrain(x-1,y+1))) c++
        if (match(getTerrain(x+1,y-1))) c++
        if (match(getTerrain(x+1,y+1))) c++
        return c
    }

    fun neighborCount(x: Int, y: Int, dirs: List<XY>, match: (x: Int, y: Int)->Boolean): Int {
        var c =0
        dirs.forEach { dir ->
            if (boundsCheck(x + dir.x, y + dir.y)) {
                if (match(x + dir.x, y + dir.y)) c++
            }
        }
        return c
    }

    protected fun cardinalBlockerCount(x: Int, y: Int): Int {
        var c = 0
        if (!isWalkableAt(x-1,y)) c++
        if (!isWalkableAt(x+1,y)) c++
        if (!isWalkableAt(x,y-1)) c++
        if (!isWalkableAt(x,y+1)) c++
        return c
    }

    protected fun neighborBlockerCount(x: Int, y: Int): Int {
        var c = 0
        if (!isWalkableAt(x-1,y)) c++
        if (!isWalkableAt(x+1,y)) c++
        if (!isWalkableAt(x,y-1)) c++
        if (!isWalkableAt(x,y+1)) c++
        if (!isWalkableAt(x-1,y-1)) c++
        if (!isWalkableAt(x+1,y+1)) c++
        if (!isWalkableAt(x+1,y-1)) c++
        if (!isWalkableAt(x-1,y+1)) c++
        return c
    }

    // Can we carve from xy in direction without hitting space?
    protected fun canCarve(xy: XY, dir: XY): Boolean {
        val destNext = xy + dir * 2
        val destAfter = destNext + dir
        if (innerBounds.contains(destNext)) {
            return (isRock(destNext) && isRock(destAfter))
        }
        return false
    }

    protected fun mergeRegions(newRegion: Int, regionsToMerge: List<Int>) {
        forEachCell { x, y ->
            if (regionsToMerge.contains(regionAt(x,y))) {
                 setRegionAt(x, y, newRegion)
            }
        }
    }

    protected fun regionsTouching(x: Int, y: Int): Set<Int> {
        val regions = mutableSetOf<Int>()
        for (dir in CARDINALS) {
            regionAt(x+ dir.x, y+ dir.y)?.also { regions.add(it) }
        }
        return regions
    }
    protected fun regionsTouching(xy: XY): Set<Int> = regionsTouching(xy.x, xy.y)

    protected fun findConnectorWalls(): Set<XY> {
        val connections = mutableSetOf<XY>()
        forEachCell { x, y ->
            if (isRock(x, y)) {
                if (regionsTouching(x, y).size > 1) {
                    connections.add(XY(x,y))
                }
            }
        }
        return connections
    }

    protected fun removeDeadEnds(type: Terrain.Type = Terrain.Type.TERRAIN_BRICKWALL): Boolean {
        var removed = false
        forEachCell { x, y ->
            if (!isRock(x, y)) {
                if (cardinalBlockerCount(x, y) == 3) {
                    setTerrain(x, y, type)
                    removed = true
                }
            }
        }
        return removed
    }

    protected fun getPrefab(): Prefab {
        val tiledFile = json.decodeFromString<TiledFile>(
            Gdx.files.internal("prefabs/building1.json").readString()
        )
        val prefab = Prefab(tiledFile)
        return prefab
    }

    protected fun addWorldPortal(building: Building, worldDest: XY, portalType: Terrain.Type, exitMsg: String): XY {
        val door = findEdgeForWorldPortal(building.facing)
        carve(door.x, door.y, 0, portalType)
        chunk.exits.add(Chunk.ExitRecord(
            Chunk.ExitType.WORLD, door,
            exitMsg,
            worldDest = worldDest
        ))
        return door
    }

    protected fun findEdgeForWorldPortal(facing: XY): XY {
        for (ny in y0 .. y1) {
            for (nx in x0..x1) {
                val x = when (facing) {
                    NORTH -> nx
                    SOUTH -> nx
                    EAST -> height - ny
                    WEST -> ny
                    else -> 0
                }
                val y = when (facing) {
                    NORTH -> ny
                    SOUTH -> height - ny
                    EAST -> nx
                    WEST -> width - nx
                    else -> 0
                }
                if (isRock(x, y) && cardinalBlockerCount(x, y) < 4 && !isRock(x - facing.x, y - facing.y)) {
                    return XY(x ,y)
                }
            }
        }
        throw RuntimeException("Can't find edge for door!")
    }

    protected fun setRoofedInRock() {
        for (y in y0  .. y1) {
            for (x in x0 .. x1) {
                if (isRock(x, y) && neighborBlockerCount(x, y) < 8) {
                    chunk.setRoofed(x, y, Chunk.Roofed.OUTDOOR)
                }
            }
        }
    }

    // Cartos should always run this at the end to overlap floor tiles and occlude shadows.
    protected fun setOverlaps() {
        for (y in y0..y1) {
            for (x in x0..x1) {
                val terrain = Terrain.get(getTerrain(x,y))
                if (terrain is Floor) {
                    val quadData = terrain.makeOverlaps(chunk,x,y)
                    setTerrainData(x, y, quadData)
                }
            }
        }
    }

    fun boundsCheck(x: Int, y: Int) = (x >= x0 && y >= y0 && x <= x1 && y <= y1)
    fun innerBoundsCheck(x: Int, y: Int) = (x > x0 && y > y0 && x < x1 && y < y1)

    protected fun forEachCellWhere(condition: (x: Int, y: Int)->Boolean, doThis: (x: Int, y: Int)->Unit) {
        forEachCell { x,y ->
            if (condition(x,y)) doThis(x,y)
        }
    }

    protected fun forEachTerrain(type: Terrain.Type, doThis: (x: Int, y: Int)->Unit) {
        forEachCell { x,y ->
            if (getTerrain(x,y) == type) doThis(x,y)
        }
    }

    protected fun neighborsAt(x: Int, y: Int, dirs: List<XY>, doThis: (nx: Int, ny: Int, terrain: Terrain.Type)->Unit) {
        dirs.forEach { dir ->
            if (boundsCheck(x + dir.x, y + dir.y)) {
                doThis(x + dir.x, y + dir.y, getTerrain(x + dir.x, y + dir.y))
            }
        }
    }

    protected fun randomFill(x0: Int, y0: Int, x1: Int, y1: Int, density: Float, fill: Terrain.Type) {
        for (x in x0..x1) {
            for (y in y0..y1) {
                if (Dice.chance(density)) setTerrain(x,y,fill)
            }
        }
    }

    protected fun evolve(gens: Int, trueType: Terrain.Type, falseType: Terrain.Type, test: (x: Int, y: Int)->Boolean) {
        repeat (gens) {
            val changes = mutableListOf<Pair<XY, Boolean>>()
            forEachCell { x, y ->
                if (x > x0 && y > y0 && x < x1 && y < y1) {
                    val isTrue = getTerrain(x, y) == trueType
                    if (test(x, y)) {
                        if (!isTrue) changes.add(Pair(XY(x, y), true))
                    } else {
                        if (isTrue) changes.add(Pair(XY(x, y), false))
                    }
                }
            }
            changes.forEach {
                setTerrain(it.first.x, it.first.y, if (it.second) trueType else falseType)
            }
        }
    }

    protected fun fuzzTerrain(type: Terrain.Type, density: Float, exclude: Terrain.Type? = null) {
        val adds = ArrayList<XY>()
        forEachTerrain(type) { x, y ->
            neighborsAt(x,y,CARDINALS) { nx, ny, terrain ->
                if (terrain != type && (exclude == null || terrain != exclude)) {
                    if (Dice.chance(density)) adds.add(XY(nx,ny))
                }
            }
        }
        adds.forEach { setTerrain(it.x, it.y, type) }
    }

    fun varianceFuzzTerrain(type: Terrain.Type, exclude: Terrain.Type? = null) {
        val adds = ArrayList<XY>()
        forEachTerrain(type) { x,y ->
            neighborsAt(x,y,CARDINALS) { nx, ny, terrain ->
                if (terrain != type && (exclude == null || terrain != exclude)) {
                    if (Dice.chance(NoisePatches.get("metaVariance", x, y).toFloat())) {
                        adds.add(XY(nx, ny))
                    }
                }
            }
        }
        adds.forEach { setTerrain(it.x, it.y, type) }
    }

    protected fun swapTerrain(oldType: Terrain.Type, newType: Terrain.Type) {
        forEachTerrain(oldType) { x,y -> setTerrain(x,y,newType) }
    }

    fun fringeTerrain(type: Terrain.Type, withType: Terrain.Type, density: Float, exclude: Terrain.Type? = null) {
        Evolver(CHUNK_SIZE, CHUNK_SIZE, false, { x,y ->
            getTerrain(x+x0,y+y0) == type
        }, { x,y ->
            setTerrain(x+x0,y+y0,withType)
        }, { x,y,n ->
            n > 0 && Dice.chance(density) && getTerrain(x+x0,y+y0) != type
        }).evolve(1)
    }

    protected fun deepenWater() {
        val adds = mutableSetOf<XY>()
        forEachTerrain(Terrain.Type.GENERIC_WATER) { x,y ->
            val shores = neighborCount(x,y) { it != Terrain.Type.GENERIC_WATER && it != Terrain.Type.TERRAIN_SHALLOW_WATER && it != Terrain.Type.BLANK }
            if (Dice.chance(shores * 0.75f)) adds.add(XY(x,y))
        }
        adds.forEach { setTerrain(it.x,it.y, Terrain.Type.TERRAIN_SHALLOW_WATER)}
        var extendChance = 0.4f
        repeat (3) {
            adds.clear()
            forEachTerrain(Terrain.Type.GENERIC_WATER) { x,y ->
                val shallows = neighborCount(x,y) { it == Terrain.Type.TERRAIN_SHALLOW_WATER }
                if (shallows > 0 && Dice.chance(extendChance)) adds.add(XY(x,y))
            }
            extendChance -= 0.15f
            adds.forEach { setTerrain(it.x,it.y, Terrain.Type.TERRAIN_SHALLOW_WATER)}
        }
        adds.clear()
        // Fill remainder with deep
        forEachTerrain(Terrain.Type.GENERIC_WATER) { x, y ->
            setTerrain(x,y, Terrain.Type.TERRAIN_DEEP_WATER)
        }
    }

    fun growBlob(width: Int, height: Int): Array<Array<Boolean>> {
        val grid = Array(width) { Array(height) { false } }
        grid[width/2][height/2] = true
        repeat (10) { growBlobStep(grid, 1, 0.2f) }
        var hitEdges = false
        while (!hitEdges) {
            for (x in 0 until width) if (grid[x][0] || grid[x][height-1]) hitEdges = true
            for (y in 0 until height) if (grid[0][y] || grid[width-1][y]) hitEdges = true
            growBlobStep(grid, 2, 0.5f)
        }
        for (x in 0 until width) {
            for (y in 0 until height) {
                var n = 0
                CARDINALS.from(x,y) { dx, dy, _ ->
                    if (dx >= 0 && dy >= 0 && dx < width && dy < height && grid[dx][dy]) n++
                }
                if (n == 4 && !grid[x][y]) grid[x][y] = true
                if (n == 0 && grid[x][y]) grid[x][y] = false
            }
        }
        return grid
    }

    private fun growBlobStep(grid: Array<Array<Boolean>>, threshold: Int, density: Float) {
        val adds = ArrayList<XY>()
        for (x in 0 until grid.size) {
            for (y in 0 until grid[0].size) {
                if (!grid[x][y]) {
                    var n = 0
                    DIRECTIONS.from(x, y) { dx, dy, _ ->
                        if (dx >= 0 && dy >= 0 && dx < grid.size && dy < grid[0].size && grid[dx][dy]) n++
                    }
                    if (n >= threshold && Dice.chance(density)) adds.add(XY(x, y))
                }
            }
        }
        adds.forEach { grid[it.x][it.y] = true }
    }

    fun growOblong(width: Int, height: Int): Array<Array<Boolean>> {
        val grid = Array(width) { Array(height) { true } }
        for (x in 0 until width) {
            grid[x][0] = false
            grid[x][height-1] = false
        }
        for (y in 0 until height) {
            grid[0][y] = false
            grid[width-1][y] = false
        }
        grid[1][1] = false
        grid[width-2][height-2] = false
        grid[1][height-2] = false
        grid[width-2][1] = false
        val adds = mutableSetOf<XY>()
        repeat ((height + width) / 4) {
            for (x in 0 until width) {
                for (y in 0 until height) {
                    if (grid[x][y]) {
                        var neighbors = 0
                        for (ix in -1 .. 1) {
                            for (iy in -1 .. 1) {
                                val nx = ix + x
                                val ny = iy + y
                                if (nx >= 0 && ny >= 0 && nx < width && ny < height && !grid[nx][ny]) {
                                    neighbors++
                                }
                            }
                        }
                        if (Dice.chance(0.06f * neighbors)) adds.add(XY(x, y))
                    }
                }
            }
            adds.forEach { grid[it.x][it.y] = false }
            adds.clear()
        }
        for (x in 0 until width) {
            for (y in 0 until height) {
                var neighbors = 0
                CARDINALS.forEach { dir ->
                    val nx = dir.x + x
                    val ny = dir.y + y
                    if (nx >= 0 && ny >= 0 && nx < width && ny < height && !grid[nx][ny]) {
                        neighbors++
                    }
                }
                if (grid[x][y] && neighbors == 4) {
                    grid[x][y] = false
                } else if (!grid[x][y] && neighbors == 0) {
                    grid[x][y] = true
                }
            }
        }
        return grid
    }

    fun printGrid(blob: Array<Array<Boolean>>, x: Int, y: Int, terrain: Terrain.Type) {
        for (ix in x..x + blob.size - 1) {
            for (iy in y .. y + blob[0].size - 1) {
                if (blob[ix - x][iy - y]) {
                    if (boundsCheck(ix, iy)) {
                        setTerrain(ix, iy, terrain)
                    }
                }
            }
        }
    }

    protected fun debugBorders() {
        carveRoom(Rect(x0, y0, x1, y0), 0, Terrain.Type.TERRAIN_STONEFLOOR)
        carveRoom(Rect(x0, y0, x0, y1), 0, Terrain.Type.TERRAIN_STONEFLOOR)
    }

    protected fun connectBuilding(building: Building) {
        log.info("Connecting new building $building")
        LevelKeeper.makeBuilding(building)
        chunk.exits.add(Chunk.ExitRecord(
            Chunk.ExitType.LEVEL, building.xy,
            building.doorMsg(),
            buildingId = building.id,
            buildingFirstLevelId = building.firstLevelId
        ))
    }

    protected fun setAllRoofed(roofed: Chunk.Roofed) {
        forEachCell { x,y ->
            chunk.setRoofed(x, y, roofed)
        }
    }

    protected fun getPlant(biome: Biome, habitat: Habitat, fertility: Float, globalPlantDensity: Float): Thing? {
        val plantChance = biome.plantDensity() * globalPlantDensity * java.lang.Float.max(0.2f, fertility)
        if (Dice.chance(plantChance)) {
            val plants = plantSpawns.filter {
                it.biomes.contains(biome) && it.habitats.contains(habitat) &&
                        fertility >= it.minFertility && fertility <= it.maxFertility
            }
            if (plants.isNotEmpty()) {
                val totalFreq = plants.map { it.frequency }.reduce { t, f -> t + f }
                val spawnFreq = Dice.float(0f, totalFreq)
                var acc = 0f
                plants.forEach {
                    acc += it.frequency
                    if (acc >= spawnFreq) {
                        return it.spawn()
                    }
                }
            }
        }
        return null
    }

}
