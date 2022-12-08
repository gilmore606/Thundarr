package world.gen.cartos

import com.badlogic.gdx.Gdx
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ktx.async.KtxAsync
import kotlin.random.Random
import things.Thing
import util.*
import world.Chunk
import world.level.Level
import world.gen.prefabs.Prefab
import world.gen.prefabs.TiledFile
import world.level.CHUNK_SIZE
import world.terrains.Floor
import world.terrains.Terrain
import world.terrains.TerrainData
import java.io.File
import java.lang.Math.abs

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

    init {
        for (x in x0 .. x1) {
            for (y in y0 .. y1) {
                chunk.setTerrain(x, y, Terrain.Type.TERRAIN_BRICKWALL)
            }
        }
    }

    protected fun getTerrain(x: Int, y: Int) = chunk.getTerrain(x,y)
    protected fun setTerrain(x: Int, y: Int, type: Terrain.Type) = chunk.setTerrain(x, y, type, Terrain.get(type).isOpaque())
    protected fun setTerrainData(x: Int, y: Int, data: TerrainData) = chunk.setTerrainData(x, y, data)
    protected fun isWalkableAt(x: Int, y: Int) = chunk.isWalkableAt(x, y)

    fun addThing(x: Int, y: Int, thing: Thing) {
        val dest = chunk.cellContainerAt(x, y)
        dest?.reconnect(level, x, y)
        thing.moveTo(dest)
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

    protected fun neighborCount(x: Int, y: Int, type: Terrain.Type): Int {
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

    protected fun neighborCount(x: Int, y: Int, match: (Terrain.Type)->Boolean): Int {
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

    protected fun findEdgeForDoor(facing: XY): XY {
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

    protected fun drawRiver(start: XY, end: XY, startWidth: Int, endWidth: Int, wiggle: Float) {
        val stepX: Float
        val stepY: Float
        var stepsLeft: Int
        var width = startWidth.toFloat()
        if (start.x == 0) {
            start.x += startWidth / 2
        } else if (start.y == 0) {
            start.y += startWidth / 2
        } else if (start.x == CHUNK_SIZE - 1) {
            start.x -= startWidth / 2
        } else if (start.y == CHUNK_SIZE - 1) {
            start.y -= startWidth / 2
        }
        val diff = end - start
        if (abs(diff.x) > abs(diff.y)) {
            stepX = diff.x / abs(diff.x).toFloat()
            stepY = diff.y / abs(diff.x).toFloat()
            stepsLeft = abs(diff.x)
        } else {
            stepX = diff.x / abs(diff.y).toFloat()
            stepY = diff.y / abs(diff.y).toFloat()
            stepsLeft = abs(diff.y)
        }
        val stepWidth = (endWidth - startWidth) / stepsLeft.toFloat()
        var cursorX = start.x.toFloat()
        var cursorY = start.y.toFloat()
        while (stepsLeft > 0) {
            carveRoom(Rect((x0 + cursorX - width / 2).toInt(), (y0 + cursorY - width / 2).toInt(),
                x0 + cursorX.toInt() + width.toInt(), y0 + cursorY.toInt() + width.toInt()),
                0, Terrain.Type.GENERIC_WATER, (width >= 3f))
            cursorX += stepX
            cursorY += stepY
            stepsLeft--
            width += stepWidth
        }
    }

    protected fun boundsCheck(x: Int, y: Int) = (x >= x0 && y >= y0 && x <= x1 && y <= y1)

    protected fun fuzzTerrain(type: Terrain.Type, density: Float, exclude: Terrain.Type? = null) {
        forEachCell { x, y ->
            if (getTerrain(x,y) == type) {
                CARDINALS.forEach { dir ->
                    if (boundsCheck(x + dir.x, y + dir.y) && getTerrain(x + dir.x, y + dir.y) != type) {
                        if (exclude == null || getTerrain(x + dir.x, y + dir.y) != exclude) {
                            if (Dice.chance(density)) {
                                setTerrain(x + dir.x, y + dir.y, type)
                            }
                        }
                    }
                }
            }
        }
    }

    // TODO: make this better so it doesn't favor down-and-right
    protected fun fringeTerrain(type: Terrain.Type, withType: Terrain.Type, density: Float, exclude: Terrain.Type? = null) {
        forEachCell { x, y ->
            if (getTerrain(x,y) == type) {
                DIRECTIONS.forEach { dir ->
                    if (boundsCheck(x + dir.x, y + dir.y) && getTerrain(x + dir.x, y + dir.y) != type) {
                        if (exclude == null || getTerrain(x + dir.x, y + dir.y) != exclude) {
                            if (Dice.chance(density)) {
                                setTerrain(x + dir.x, y + dir.y, withType)
                            }
                        }
                    }
                }
            }
        }
    }

    protected fun deepenWater() {
        val adds = mutableSetOf<XY>()
        forEachCell { x, y ->
            if (getTerrain(x,y) == Terrain.Type.GENERIC_WATER) {
                val shores = neighborCount(x,y) { it != Terrain.Type.GENERIC_WATER && it != Terrain.Type.TERRAIN_SHALLOW_WATER && it != Terrain.Type.BLANK }
                if (Dice.chance(shores * 0.75f)) adds.add(XY(x,y))
            }
        }
        adds.forEach { setTerrain(it.x,it.y, Terrain.Type.TERRAIN_SHALLOW_WATER)}
        var extendChance = 0.4f
        repeat (3) {
            adds.clear()
            forEachCell { x, y ->
                if (getTerrain(x,y) == Terrain.Type.GENERIC_WATER) {
                    val shallows = neighborCount(x,y) { it == Terrain.Type.TERRAIN_SHALLOW_WATER }
                    if (shallows > 0) {
                        if (Dice.chance(extendChance)) adds.add(XY(x,y))
                    }
                }
            }
            extendChance -= 0.15f
            adds.forEach { setTerrain(it.x,it.y, Terrain.Type.TERRAIN_SHALLOW_WATER)}
        }
        adds.clear()
        // Fill remainder with deep
        forEachCell { x, y ->
            if (getTerrain(x,y) == Terrain.Type.GENERIC_WATER) {
                setTerrain(x,y, Terrain.Type.TERRAIN_DEEP_WATER)
            }
        }
    }

    protected fun debugBorders() {
        carveRoom(Rect(x0, y0, x1, y0), 0, Terrain.Type.TERRAIN_STONEFLOOR)
        carveRoom(Rect(x0, y0, x0, y1), 0, Terrain.Type.TERRAIN_STONEFLOOR)
    }
}
