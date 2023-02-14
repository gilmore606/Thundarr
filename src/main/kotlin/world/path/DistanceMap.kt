package world.path

import util.DIRECTIONS
import util.XY
import util.from
import world.Chunk
import world.level.CHUNK_SIZE

// A distance map on an arbitrary chunk to an arbitrary point.
// Useful for level gen, and maybe other things.

class DistanceMap(
    val chunk: Chunk,
    val isTarget: (x: Int, y: Int)->Boolean
) {
    val map: Array<Array<Int>> = Array(chunk.width) { Array(chunk.height) { -1 } }
    var maxDistance = 0

    init {
        var step = 0
        for (ix in 0 until chunk.width) {
            for (iy in 0 until chunk.height) {
                if (isTarget(ix + chunk.x, iy + chunk.y)) map[ix][iy] = step
            }
        }
        var dirty = true
        while (dirty) {
            dirty = false
            for (x in 0 until chunk.width) {
                for (y in 0 until chunk.height) {
                    if (map[x][y] == step) {
                        DIRECTIONS.from(x, y) { tx, ty, dir ->
                            if (tx in 0 until chunk.width && ty in 0 until chunk.height) {
                                if (map[tx][ty] < 0) {
                                    if (chunk.isWalkableAt(chunk.x + tx, chunk.y + ty)) {
                                        map[tx][ty] = step + 1
                                        dirty = true
                                    }
                                }
                            }
                        }
                    }
                }
            }
            step++
        }
        maxDistance = step - 1
    }

    fun distanceAt(x: Int, y: Int) = if (chunk.inBounds(x, y)) map[x - chunk.x][y - chunk.y] else -1

}
