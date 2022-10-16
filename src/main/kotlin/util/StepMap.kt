package util

// This should be DijkstraMap but who wants to type that guy's name all the time?

class StepMap(
    val width: Int, val height: Int,
    val isWalkableAt: (Int,Int)->Boolean,
    val xOffset: ()->Int,
    val yOffset: ()->Int,
) {

    private val map = Array(width) { Array(height) { -1 } }


    fun update(povX: Int, povY: Int) {
        clear()
        var step = 0
        val localPov = XY(povX - xOffset(), povY - yOffset())
        map[localPov.x][localPov.y] = step
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
                                    if (isWalkableAt(x + dir.x + xOffset(), y + dir.y + yOffset())) {
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
        return stepFromLocal(XY(from.x - xOffset(), from.y - yOffset()))
    }

    private fun stepFromLocal(local: XY): XY? {
        val feet = XY(local.x, local.y)
        val step = map[feet.x][feet.y]
        return when (step) {
            -1 -> null
            0 -> NO_DIRECTION
            else -> DIRECTIONS.firstOrNull { dir ->
                val tx = feet.x + dir.x
                val ty = feet.y + dir.y
                if (tx >= 0 && ty >= 0 && tx < width && ty < height) {
                    map[tx][ty] == step - 1
                } else false
            }
        }
    }

    fun pathFrom(from: XY): List<XY> {
        val path = ArrayList<XY>()
        return path
        val localFrom = XY(from.x - xOffset(), from.y - yOffset())
        var feet = XY(localFrom.x, localFrom.y)
        var stepNum = map[feet.x][feet.y]
        while (stepNum >= 0) {
            path.add(XY(feet.x + xOffset(), feet.y + yOffset()))
            stepFromLocal(feet)?.also { step ->
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
