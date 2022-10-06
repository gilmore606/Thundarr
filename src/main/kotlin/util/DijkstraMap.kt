package util

import world.Level

class DijkstraMap(val level: Level) {

    val map = Array(level.width) { Array(level.height) { -1 } }

    fun clear() {
        for (x in 0 until level.width) {
            for (y in 0 until level.height) {
                map[x][y] = -1
            }
        }
    }

    fun update(pov: XY) {
        fun markStep(x: Int, y: Int, step: Int): Boolean {
            if (level.isWalkableAt(x, y)) {
                map[x][y] = step
                return true
            }
            return false
        }

        clear()
        var step = 0
        map[pov.x][pov.y] = step
        var dirty = true
        while (dirty) {
            dirty = false
            for (x in 0 until level.width) {
                for (y in 0 until level.height) {
                    if (map[x][y] == step) {
                        DIRECTIONS.forEach { dir ->
                            if (map[x + dir.x][y + dir.y] < 0) {
                                val thisDirty = markStep(x + dir.x, y + dir.y, step + 1)
                                dirty = dirty || thisDirty
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
        var step = map[feet.x][feet.y]
        while (step >= 0) {
            path.add(XY(feet.x, feet.y))
            var stepped = false
            DIRECTIONS.forEach { dir ->
                if (!stepped) {
                    if (map[feet.x + dir.x][feet.y + dir.y] == step - 1) {
                        feet += dir
                        step--
                        stepped = true
                    }
                }
            }
            if (!stepped) {
                // Hit a dead end.
                path.clear()
                step = -1
            }
        }
        return path
    }
}
