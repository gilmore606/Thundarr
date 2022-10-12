package world.cartos

import util.CARDINALS
import kotlin.random.Random
import render.tilesets.Glyph
import util.XY
import world.Level
import util.Rect
import util.log
import world.EnclosedLevel
import world.terrains.Terrain

abstract class Carto {

    protected lateinit var regions: Array<Array<Int?>>

    private var x0 = 0
    private var y0 = 0
    private var x1 = 0
    private var y1 = 0
    private var bounds = Rect(x0,y0,x1,y1)
    private var innerBounds = Rect(x0+1,y0+1,x1-1,y1-1)
    private var width = 0
    private var height = 0
    private lateinit var getTerrain: (Int, Int)->Terrain.Type
    private lateinit var setTerrain: (Int, Int, Terrain.Type)->Unit

    fun carveLevel(x0: Int, y0: Int, x1: Int, y1: Int,
                            getTerrain: (Int,Int)->Terrain.Type,
                            setTerrain: (Int,Int,Terrain.Type)->Unit) {
        this.x0 = x0
        this.y0 = y0
        this.x1 = x1
        this.y1 = y1
        this.bounds = Rect(x0,y0,x1,y1)
        this.innerBounds = Rect(x0+1,y0+1,x1-1,y1-1)
        this.width = x1 - x0
        this.height = y1 - y0
        this.getTerrain = getTerrain
        this.setTerrain = setTerrain
        regions = Array(1+x1-x0) { Array(1+y1-y0) { null } }
        doCarveLevel()
    }

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
        return !Terrain.get(getTerrain(x, y)).isWalkable()
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
}
