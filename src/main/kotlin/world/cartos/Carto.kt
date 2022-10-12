package world.cartos

import util.CARDINALS
import kotlin.random.Random
import render.tilesets.Glyph
import util.XY
import world.Level
import util.Rect
import world.terrains.Terrain

abstract class Carto {

    protected lateinit var level: Level
    protected lateinit var regions: Array<Array<Int?>>

    abstract fun carveLevel()

    fun makeLevel(): Level {
        val width = 81
        val height = 81
        level = Level(width, height)
        regions = Array(width) { Array(height) { null } }
        carveLevel()
        return level
    }

    protected fun regionAt(x: Int, y: Int): Int? = try {
        regions[x][y]
    } catch (e: ArrayIndexOutOfBoundsException) { null }
    protected fun regionAt(xy: XY): Int? = regionAt(xy.x, xy.y)

    // Can the given tile be carved into?
    protected fun isRock(x: Int, y: Int): Boolean {
        return level.getGlyph(x, y) == Glyph.WALL
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
        val x = Random.nextInt((level.width - width) / 2) * 2 + 1
        val y = Random.nextInt((level.height - height) / 2) * 2 + 1
        return Rect(x, y, x + width - 1, y + height - 1)
    }

    // TODO: don't pass regionId here, compute it automatically in a stage
    // where we floodfill successively until all space is regions
    protected fun carve(x: Int, y: Int, regionId: Int,
                        type: Terrain.Type = Terrain.Type.TERRAIN_STONEFLOOR) {
        level.setTerrain(x, y, type)
        regions[x][y] = regionId
    }
    protected fun carve(xy: XY, regionId: Int,
                        type: Terrain.Type = Terrain.Type.TERRAIN_STONEFLOOR) {
        carve(xy.x, xy.y, regionId, type)
    }

    protected fun carveRoom(room: Rect, regionId: Int,
                            type: Terrain.Type = Terrain.Type.TERRAIN_STONEFLOOR) {
        for (x in room.x0..room.x1) {
            for (y in room.y0..room.y1) {
                level.setTerrain(x, y, type)
                regions[x][y] = regionId
            }
        }
    }

    protected fun forEachCell(doThis: (x: Int, y: Int) -> Unit) {
        for (x in 0 until level.width) {
            for (y in 0 until level.height) {
                doThis(x,y)
            }
        }
    }

    protected fun neighborCount(x: Int, y: Int, type: Glyph): Int {
        var c = 0
        if (level.getGlyph(x-1,y) == type) c++
        if (level.getGlyph(x+1,y) == type) c++
        if (level.getGlyph(x-1,y-1) == type) c++
        if (level.getGlyph(x,y-1) == type) c++
        if (level.getGlyph(x+1,y-1) == type) c++
        if (level.getGlyph(x-1,y+1) == type) c++
        if (level.getGlyph(x,y+1) == type) c++
        if (level.getGlyph(x+1,y+1) == type) c++
        return c
    }

    protected fun cardinalCount(x: Int, y: Int, type: Glyph): Int {
        var c = 0
        if (level.getGlyph(x-1,y) == type) c++
        if (level.getGlyph(x+1,y) == type) c++
        if (level.getGlyph(x,y-1) == type) c++
        if (level.getGlyph(x,y+1) == type) c++
        return c
    }

    // Can we carve from xy in direction without hitting space?
    protected fun canCarve(xy: XY, dir: XY): Boolean {
        val destNext = xy + dir * 2
        val destAfter = destNext + dir
        if (Rect(1, 1, level.width - 2, level.height - 2).contains(destNext)) {
            return (isRock(destNext) && isRock(destAfter))
        }
        return false
    }

    protected fun mergeRegions(newRegion: Int, regionsToMerge: List<Int>) {
        forEachCell { x, y ->
            if (regionsToMerge.contains(regions[x][y])) {
                regions[x][y] = newRegion
            }
        }
    }

    protected fun regionsTouching(xy: XY): Set<Int> {
        val regions = mutableSetOf<Int>()
        for (dir in CARDINALS) {
            regionAt(xy + dir)?.also { regions.add(it) }
        }
        return regions
    }
    protected fun regionsTouching(x: Int, y: Int): Set<Int> = regionsTouching(XY(x, y))

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
                if (cardinalCount(x, y, Glyph.WALL) == 3) {
                    level.setTerrain(x, y, Terrain.Type.TERRAIN_BRICKWALL)
                    removed = true
                }
            }
        }
        return removed
    }
}
