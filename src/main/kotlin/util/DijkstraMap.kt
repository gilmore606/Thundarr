package util

import world.EnclosedLevel
import world.Level


class DijkstraMap(val width: Int, val height: Int,
                    val isWalkableAt: (Int,Int)->Boolean) {

    private val map = Array(width) { Array(height) { -1 } }


    fun update(pov: XY) {
        clear()
        var step = 0
        map[pov.x][pov.y] = step
        var dirty = true
        while (dirty) {
            dirty = false
            for (x in 0 until width) {
                for (y in 0 until height) {
                    if (map[x][y] == step) {
                        DIRECTIONS.forEach { dir ->
                            try {
                                if (map[x + dir.x][y + dir.y] < 0) {
                                    if (isWalkableAt(x + dir.x, y + dir.y)) {
                                        map[x + dir.x][y + dir.y] = step + 1
                                        dirty = true
                                    }
                                }
                            } catch (_: ArrayIndexOutOfBoundsException) { }
                        }
                    }
                }
            }
            step++
        }
    }

    fun stepFrom(from: XY): XY? {
        val feet = XY(from.x, from.y)
        val step = map[feet.x][feet.y]
        return when (step) {
            -1 -> null
            0 -> NO_DIRECTION
            else -> DIRECTIONS.firstOrNull { dir ->
                try {
                    map[feet.x + dir.x][feet.y + dir.y] == step - 1
                } catch (_: ArrayIndexOutOfBoundsException) { false }
            }
        }
    }

    fun pathFrom(from: XY): List<XY> {
        val path = ArrayList<XY>()
        var feet = XY(from.x, from.y)
        var stepNum = map[feet.x][feet.y]
        while (stepNum >= 0) {
            path.add(XY(feet.x, feet.y))
            stepFrom(feet)?.also { step ->
                feet += step
                stepNum--
            } ?: run {
                path.clear()
                return path
            }
        }
        return path
    }

    private fun clear() {
        for (x in 0 until width) {
            for (y in 0 until height) {
                map[x][y] = -1
            }
        }
    }
}
