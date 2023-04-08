package world.history

import util.DIRECTIONS
import util.from
import world.gen.Metamap

// A distance map on the Metamap

class MetaStepMap {
    val size = Metamap.chunkRadius * 2
    val map = Array(size) { Array(size) { -1 } }

    fun reset() {
        for (ix in 0 until size) {
            for (iy in 0 until size) {
                map[ix][iy] = -1
            }
        }
    }

    fun addTarget(x: Int, y: Int) {
        map[x][y] = 0
    }

    fun update() {
        var step = 0
        var dirty = true
        while (dirty) {
            dirty = false
            for (x in 0 until size) {
                for (y in 0 until size) {
                    if (map[x][y] == -1) dirty = true
                    if (map[x][y] == step) {
                        DIRECTIONS.from(x, y) { tx, ty, dir ->
                            if (tx in 0 until size && ty in 0 until size) {
                                if (map[tx][ty] < 0) {
                                    map[tx][ty] = step + (Metamap.metaAt(tx,ty).biome.metaTravelCost() * 4).toInt()
                                }
                            }
                        }
                    }
                }
            }
            step++
        }
    }

    fun distanceAt(x: Int, y: Int) = map[x][y]
}
