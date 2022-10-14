package util

import java.lang.RuntimeException


class ShadowCaster() {

    class Shadow {
        var start = 0f
        var end = 1f
        fun contains(other: Shadow) = (start <= other.start && end >= other.end)
    }

    class ShadowLine(private val octant: Int) {
        val transform = XY(0,0)
        private val maxShadows = 30
        private val shadows = mutableListOf<Shadow>()
        private val shadowCache = mutableListOf<Shadow>().apply {
            repeat (maxShadows) { add(Shadow()) }
        }

        fun reset() {
            shadowCache.addAll(shadows)
            shadows.clear()
            transform.x = 0
            transform.y = 0
        }

        fun isInShadow(projection: Shadow) =
            shadows.firstOrNull { it.contains(projection) }?.let { true } ?: false

        fun isFullShadow() =
            (shadows.size == 1 && shadows[0].start == 0f && shadows[0].end == 1f)

        private fun removeAt(i: Int) {
            shadowCache.add(shadows.removeAt(i))
        }

        fun add(shadow: Shadow) {
            val i: Int = shadows.indexOfFirst { it.start >= shadow.start }
            val index = if (i == -1) shadows.size else i
            var overlapPrevious: Shadow? = null
            if (index > 0 && shadows[index - 1].end > shadow.start) {
                overlapPrevious = shadows[index - 1]
            }
            var overlapNext: Shadow? = null
            if (index < shadows.size && shadows[index].start < shadow.end) {
                overlapNext = shadows[index]
            }
            var added = false
            overlapNext?.also { next ->
                overlapPrevious?.also { previous ->
                    previous.end = next.end
                    removeAt(index)
                } ?: run {
                    next.start = shadow.start
                }
            } ?: run {
                overlapPrevious?.also { previous ->
                    previous.end = shadow.end
                } ?: run {
                    shadows.add(index, shadow)
                    added = true
                }
            }
            if (!added) discard(shadow)
        }

        fun discard(shadow: Shadow) = shadowCache.add(shadow)

        fun projectTile(row: Int, col: Int) = shadowCache.removeAt(0).apply {
            start = col.toFloat() / (row.toFloat() + 2f)
            end = (col.toFloat() + 1f) / (row.toFloat() + 1f)
        }

        fun transformOctant(row: Int, col: Int) {
            when (octant) {
                0 -> { transform.x = col; transform.y = -row }
                1 -> { transform.x = row; transform.y = -col }
                2 -> { transform.x = row; transform.y = col }
                3 -> { transform.x = col; transform.y = row }
                4 -> { transform.x = -col; transform.y = row }
                5 -> { transform.x = -row; transform.y = col }
                6 -> { transform.x = -row; transform.y = -col }
                7 -> { transform.x = -col; transform.y = -row }
                else -> throw RuntimeException()
            }
        }
    }

    private val lineCache = Array(8) { n -> ShadowLine(n) }


    fun castVisibility(pov: XY, distance: Float,
                       isOpaqueAt: (x: Int, y: Int) -> Boolean,
                       setVisibility: (x: Int, y: Int, visibility: Boolean) -> Unit
    ) {
        setVisibility(pov.x, pov.y, true)
        lineCache.forEach { line ->
            refreshOctant(line, pov.x, pov.y, distance, isOpaqueAt, setVisibility)
        }
    }

    fun castLight(x: Int, y: Int, lightColor: LightColor,
                  isOpaqueAt: (x: Int, y: Int) -> Boolean,
                  setLight: (x: Int, y: Int, r: Float, g: Float, b: Float) -> Unit) {
        setLight(x, y, lightColor.r, lightColor.g, lightColor.b)
        lineCache.forEach { line ->

        }
    }

    private fun refreshOctant(line: ShadowLine, povX: Int, povY: Int, distance: Float,
                              isOpaqueAt: (x: Int, y: Int) -> Boolean,
                              setVisibility: (x: Int, y: Int, visibility: Boolean) -> Unit
    ) {
        line.reset()
        var fullShadow = false
        var row = 0
        var done = false
        while (!done) {
            row++
            line.transformOctant(row, 0)
            var castX = povX + line.transform.x
            var castY = povY + line.transform.y
            if (distanceBetween(povX, povY, castX, castY) > distance) {
                done = true
            } else {
                var doneRow = false
                var col = 0
                while (!doneRow && col <= row) {
                    line.transformOctant(row, col)
                    castX = povX + line.transform.x
                    castY = povY + line.transform.y
                    if (distanceBetween(povX, povY, castX, castY) > distance) {
                        doneRow = true
                    } else {
                        if (fullShadow) {
                            setVisibility(castX, castY, false)
                        } else {
                            val projection = line.projectTile(row, col)
                            val visible = !line.isInShadow(projection)
                            setVisibility(castX, castY, visible)
                            if (visible && isOpaqueAt(castX, castY)) {
                                line.add(projection)
                                fullShadow = line.isFullShadow()
                            } else {
                                line.discard(projection)
                            }
                        }
                    }
                    col++
                }
            }
        }
    }
}
