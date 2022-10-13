package util

// This should be DijkstraMap but who wants to type that guy's name all the time?

class StepMap(
    val width: Int, val height: Int,
    val isWalkableAt: (Int,Int)->Boolean) {

    private val map = Array(width) { Array(height) { -1 } }

    private var x0 = 0
    private var y0 = 0

    fun setOrigin(x: Int, y: Int) {
        x0 = x
        y0 = y
    }

    fun update(povX: Int, povY: Int) {
        clear()
        var step = 0
        map[povX - x0][povY - y0] = step
        var dirty = true
        while (dirty) {
            dirty = false
            for (x in 0 until width) {
                for (y in 0 until height) {
                    if (map[x][y] == step) {
                        DIRECTIONS.forEach { dir ->
                            val tx = x + dir.x
                            val ty = y + dir.y
                            if (tx >= 0 && ty >= 0 && tx < width && ty < height) {
                                if (map[x + dir.x][y + dir.y] < 0) {
                                    if (isWalkableAt(x + dir.x + x0, y + dir.y + y0)) {
                                        map[x + dir.x][y + dir.y] = step + 1
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
    }

    fun stepFrom(from: XY): XY? {
        val feet = XY(from.x, from.y)
        val step = map[feet.x - x0][feet.y - y0]
        return when (step) {
            -1 -> null
            0 -> NO_DIRECTION
            else -> DIRECTIONS.firstOrNull { dir ->
                val tx = feet.x + dir.x - x0
                val ty = feet.y + dir.y - y0
                if (tx >= 0 && ty >= 0 && tx < width && ty < height) {
                    map[tx][ty] == step - 1
                } else false
            }
        }
    }

    fun pathFrom(from: XY): List<XY> {
        val path = ArrayList<XY>()
        var feet = XY(from.x, from.y)
        var stepNum = map[feet.x - x0][feet.y - y0]
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
