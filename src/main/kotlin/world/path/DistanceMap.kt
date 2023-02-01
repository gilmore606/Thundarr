package world.path

import util.DIRECTIONS
import util.XY
import util.from
import world.Chunk

// A distance map on an arbitrary chunk to an arbitrary point.
// Useful for level gen, and maybe other things.

class DistanceMap(
    val target: XY,
    val chunk: Chunk
) {

    val map: Array<Array<Int>> = Array(chunk.width) { Array(chunk.height) { -1 } }
    var maxDistance = 0

    init {
        var step = 0
        map[target.x - chunk.x][target.y - chunk.y] = step
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
        maxDistance = step
    }

    fun distanceAt(x: Int, y: Int) = if (x in 0 until chunk.width && y in 0 until chunk.height) map[x - chunk.x][y - chunk.y] else -1

}
