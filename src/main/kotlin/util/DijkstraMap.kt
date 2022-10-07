package util

import world.Level


class DijkstraMap(val level: Level) {

    private val map = Array(level.width) { Array(level.height) { -1 } }


    fun update(pov: XY) {
        clear()
        var step = 0
        map[pov.x][pov.y] = step
        var dirty = true
        while (dirty) {
            dirty = false
            level.forEachCell { x, y ->
                if (map[x][y] == step) {
                    DIRECTIONS.forEach { dir ->
                        if (map[x + dir.x][y + dir.y] < 0) {
                            if (level.isWalkableAt(x + dir.x, y + dir.y)) {
                                map[x + dir.x][y + dir.y] = step + 1
                                dirty = true
                            }
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
                map[feet.x + dir.x][feet.y + dir.y] == step - 1
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
        level.forEachCell { x, y -> map[x][y] = -1 }
    }
}
