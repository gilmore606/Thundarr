package world.cartos

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import util.CARDINALS
import kotlin.random.Random
import things.Thing
import util.XY
import util.Rect
import world.Chunk
import world.cartos.prefabs.Prefab
import world.cartos.prefabs.TiledFile
import world.terrains.Terrain
import java.io.File

abstract class Carto {

    protected lateinit var regions: Array<Array<Int?>>

    protected var x0 = 0
    protected var y0 = 0
    protected var x1 = 0
    protected var y1 = 0
    protected var bounds = Rect(x0,y0,x1,y1)
    protected var innerBounds = Rect(x0+1,y0+1,x1-1,y1-1)
    protected var width = 0
    protected var height = 0
    protected lateinit var chunk: Chunk

    fun carveLevel(x0: Int, y0: Int, x1: Int, y1: Int, chunk: Chunk) {
        this.chunk = chunk
        this.x0 = x0
        this.y0 = y0
        this.x1 = x1
        this.y1 = y1
        this.bounds = Rect(x0,y0,x1,y1)
        this.innerBounds = Rect(x0+1,y0+1,x1-1,y1-1)
        this.width = x1 - x0
        this.height = y1 - y0
        regions = Array(1+x1-x0) { Array(1+y1-y0) { null } }
        for (x in x0 .. x1) {
            for (y in y0 .. y1) {
                chunk.setTerrain(x, y, Terrain.Type.TERRAIN_BRICKWALL)
            }
        }
        doCarveLevel()
    }

    protected fun getTerrain(x: Int, y: Int) = chunk.getTerrain(x,y)
    protected fun setTerrain(x: Int, y: Int, type: Terrain.Type) = chunk.setTerrain(x, y, type)
    protected fun isWalkableAt(x: Int, y: Int) = chunk.isWalkableAt(x, y)
    protected fun addThingAt(x: Int, y: Int, thing: Thing) = chunk.addThingAt(x, y, thing)

    protected abstract fun doCarveLevel()

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
                            type: Terrain.Type = Terrain.Type.TERRAIN_STONEFLOOR) {
        for (x in room.x0..room.x1) {
            for (y in room.y0..room.y1) {
                setTerrain(x, y, type)
                setRegionAt(x, y, regionId)
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

    protected fun cardinalCount(x: Int, y: Int, type: Terrain.Type): Int {
        var c = 0
        if (getTerrain(x-1,y) == type) c++
        if (getTerrain(x+1,y) == type) c++
        if (getTerrain(x,y-1) == type) c++
        if (getTerrain(x,y+1) == type) c++
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

    protected fun removeDeadEnds(): Boolean {
        var removed = false
        forEachCell { x, y ->
            if (!isRock(x, y)) {
                if (cardinalCount(x, y, Terrain.Type.TERRAIN_BRICKWALL) == 3) {
                    setTerrain(x, y, Terrain.Type.TERRAIN_BRICKWALL)
                    removed = true
                }
            }
        }
        return removed
    }

    protected fun getPrefab(): Prefab {
        val tiledFile = Json.decodeFromString<TiledFile>(File("resources/prefabs/building1.json").readText(Charsets.UTF_8))
        val prefab = Prefab(tiledFile)
        return prefab
    }
}
