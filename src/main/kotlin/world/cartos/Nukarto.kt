package world.cartos

import kotlinx.serialization.json.Json
import util.*
import world.Chunk
import world.cartos.prefabs.Prefab
import world.level.Level
import world.terrains.Floor
import world.terrains.Terrain

abstract class Nukarto(
    val level: Level,
    val chunk: Chunk,
    val floorType: Terrain.Type,
    val wallType: Terrain.Type
) {
    val width
        get() = chunk.width
    val height
        get() = chunk.height

    protected val json = Json { ignoreUnknownKeys = true }

    class Room(
        x: Int, y: Int, width: Int, height: Int
    )

    suspend fun carveLevel() {
        doCarveLevel()

        setTileOverlaps()  // create floor overlaps and wall shadows
    }

    abstract suspend fun doCarveLevel()

    protected fun set(x: Int, y: Int, type: Terrain.Type) {
        chunk.setTerrain(x, y, type)
    }

    protected fun get(x: Int, y: Int): Terrain.Type =
        chunk.getTerrain(x, y)

    protected fun getTerrain(x: Int, y: Int): Terrain =
        Terrain.get(chunk.getTerrain(x, y))

    protected fun isWalkableAt(x: Int, y: Int) = chunk.isWalkableAt(x, y)

    protected fun forEachCell(doThis: (x: Int, y: Int)->Unit) {
        for (x in 0 until chunk.width) {
            for (y in 0 until chunk.height) {
                doThis(x, y)
            }
        }
    }

    protected fun fillBox(x0: Int, y0: Int, x1: Int, y1: Int, type: Terrain.Type) {
        for (x in x0..x1) {
            for (y in y0 .. y1) {
                chunk.setTerrain(x, y, type)
            }
        }
    }

    protected fun cardinalBlockerCount(x: Int, y: Int): Int {
        var c = 0
        if (!chunk.isWalkableAt(x-1,y)) c++
        if (!chunk.isWalkableAt(x+1,y)) c++
        if (!chunk.isWalkableAt(x,y-1)) c++
        if (!chunk.isWalkableAt(x,y+1)) c++
        return c
    }

    protected fun neighborBlockerCount(x: Int, y: Int): Int {
        var c = 0
        if (!chunk.isWalkableAt(x-1,y)) c++
        if (!chunk.isWalkableAt(x+1,y)) c++
        if (!chunk.isWalkableAt(x,y-1)) c++
        if (!chunk.isWalkableAt(x,y+1)) c++
        if (!chunk.isWalkableAt(x-1,y-1)) c++
        if (!chunk.isWalkableAt(x+1,y+1)) c++
        if (!chunk.isWalkableAt(x+1,y-1)) c++
        if (!chunk.isWalkableAt(x-1,y+1)) c++
        return c
    }

    protected fun removeDeadEnds(tilComplete: Boolean = false) {
        var done = false
        while (!done) {
            var removedSome = false
            forEachCell { x,y ->
                if (chunk.isWalkableAt(x,y)) {
                    if (cardinalBlockerCount(x,y) == 3) {
                        set(x, y, wallType)
                        removedSome = true
                    }
                }
            }
            done = !removedSome || !tilComplete
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
                    set(x + atX, y + atY, type)
                }
            }
        }
    }

    protected fun findEdgeForDoor(facing: XY): XY {
        for (ny in 0 until height) {
            for (nx in 0 until width) {
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
                if (!isWalkableAt(x, y) && cardinalBlockerCount(x, y) < 4 && isWalkableAt(x - facing.x, y - facing.y)) {
                    return XY(x ,y)
                }
            }
        }
        throw RuntimeException("Can't find edge for door!")
    }

    private fun setTileOverlaps() {
        forEachCell { x,y ->
            val terrain = Terrain.get(chunk.getTerrain(x, y))
            if (terrain is Floor) {
                chunk.setTerrainData(x, y, terrain.makeOverlaps(chunk, x, y))
            }
        }
    }
}
