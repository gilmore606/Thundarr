package util

import actors.Actor
import world.Entity
import world.level.Level
import java.lang.RuntimeException

const val MAX_LIGHT_RANGE = 24f
const val LIGHT_BLEED = 0.25f
const val VISIBILITY_BRIGHTNESS = 0.32f

class RayCaster {

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
                       lightAt: (x: Int, y: Int) -> LightColor,
                       setVisibility: (x: Int, y: Int, visibility: Boolean
                       ) -> Unit
    ) {
        setVisibility(pov.x, pov.y, true)
        lineCache.forEach { line ->
            visifyOctant(line, pov.x, pov.y, distance, isOpaqueAt, lightAt, setVisibility)
        }
        DIRECTIONS.forEach { setVisibility(pov.x + it.x, pov.y + it.y, true) }
    }

    private fun visifyOctant(line: ShadowLine, povX: Int, povY: Int, distance: Float,
                             isOpaqueAt: (x: Int, y: Int) -> Boolean,
                             lightAt: (x: Int, y: Int) -> LightColor,
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
                            setVisibility(castX, castY, visible && (lightAt(castX, castY).brightness() >= VISIBILITY_BRIGHTNESS))
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

    fun castLight(x: Int, y: Int, lightColor: LightColor,
                  isOpaqueAt: (x: Int, y: Int) -> Boolean,
                  setLight: (x: Int, y: Int, r: Float, g: Float, b: Float) -> Unit,
                  getThisLight: (x: Int, y: Int) -> LightColor?
                  ) {
        val br = lightColor.brightness()
        setLight(x, y, lightColor.r / br, lightColor.g / br, lightColor.b / br)
        lineCache.forEach { line ->
            lightOctant(line, x, y, lightColor, br, isOpaqueAt, setLight)
        }
        // Bleed light into neighboring cells.
        val bleeds = mutableSetOf<Triple<Int, Int, LightColor>>()
        forXY(0,0, MAX_LIGHT_RANGE.toInt()*2-1, MAX_LIGHT_RANGE.toInt()*2-1) { ix,iy ->
            val tx = x + ix - MAX_LIGHT_RANGE.toInt()
            val ty = y + iy - MAX_LIGHT_RANGE.toInt()
            if (!isOpaqueAt(tx, ty)) {
                getThisLight(tx, ty) ?: run {
                    var bleed: LightColor? = null
                    CARDINALS.forEach { dir ->
                        if (!isOpaqueAt(tx + dir.x, ty + dir.y)) {
                            getThisLight(tx + dir.x, ty + dir.y)?.also { neighbor ->
                                if (bleed == null) bleed = LightColor(0f, 0f, 0f)
                                bleed!!.r += neighbor.r * LIGHT_BLEED
                                bleed!!.g += neighbor.g * LIGHT_BLEED
                                bleed!!.b += neighbor.b * LIGHT_BLEED
                            }
                        }
                    }
                    bleed?.also { bleeds.add(Triple(tx, ty, it)) }
                }
            }
        }
        bleeds.forEach { setLight(it.first, it.second, it.third.r, it.third.g, it.third.b )}
    }

    private fun lightOctant(line: ShadowLine, povX: Int, povY: Int, lightColor: LightColor, brightness: Float,
                            isOpaqueAt: (x: Int, y: Int) -> Boolean,
                            setLight: (x: Int, y: Int, r: Float, g: Float, b: Float) -> Unit
    ) {
        line.reset()
        var fullShadow = false
        var row = 0
        var done = false
        val range = MAX_LIGHT_RANGE * brightness
        while (!done) {
            row++
            line.transformOctant(row, 0)
            var castX = povX + line.transform.x
            var castY = povY + line.transform.y
            var falloff = 1f - distanceBetween(povX, povY, castX, castY) / range
            if (falloff <= 0.05f) {
                done = true
            } else {
                var doneRow = false
                var col = 0
                while (!doneRow && col <= row) {
                    line.transformOctant(row, col)
                    castX = povX + line.transform.x
                    castY = povY + line.transform.y
                    falloff = 1f - distanceBetween(povX, povY, castX, castY) / range
                    if (falloff <= 0.05f) {
                        doneRow = true
                    } else if (!fullShadow) {
                        val lightR = lightColor.r * falloff / brightness
                        val lightG = lightColor.g * falloff / brightness
                        val lightB = lightColor.b * falloff / brightness
                        val projection = line.projectTile(row, col)
                        val visible = !line.isInShadow(projection)
                        val opaque = isOpaqueAt(castX, castY)
                        if (visible) setLight(castX, castY, lightR, lightG, lightB)
                        if (visible && opaque) {
                            line.add(projection)
                            fullShadow = line.isFullShadow()
                        } else {
                            line.discard(projection)
                        }
                    }
                    col++
                }
            }
        }
    }

    fun populateSeenEntities(entities: MutableMap<Entity, Float>, seer: Actor) {
        seer.level?.also { level ->
            val range = seer.visualRange()
            lineCache.forEach { line ->
                populateSeenOctant(seer, level, entities, line, seer.xy.x, seer.xy.y, range)
            }
        }
    }

    private fun populateSeenOctant(seer: Actor, level: Level, resultSet: MutableMap<Entity, Float>, line: ShadowLine, povX: Int, povY: Int, range: Float)
    {
        line.reset()
        var fullShadow = false
        var row = 0
        var done = false
        while (!done) {
            row++
            line.transformOctant(row, 0)
            var castX = povX + line.transform.x
            var castY = povY + line.transform.y
            if (distanceBetween(povX, povY, castX, castY) > range) {
                done = true
            } else {
                var doneRow = false
                var col = 0
                while (!doneRow && col <= row) {
                    line.transformOctant(row, col)
                    castX = povX + line.transform.x
                    castY = povY + line.transform.y
                    val distance = distanceBetween(povX, povY, castX, castY)
                    if (distance > range) {
                        doneRow = true
                    } else {
                        if (!fullShadow) {
                            val projection = line.projectTile(row, col)
                            val visible = !line.isInShadow(projection) && seer.canSee(XY(castX, castY), checkOcclusion = false)
                            if (visible) {
                                // collect targets
                                level.actorAt(castX, castY)?.also {
                                    if (!it.sneakCheck(seer, distance)) resultSet[it] = distance
                                }
                                level.thingsAt(castX, castY).forEach {
                                    resultSet[it] = distance
                                }
                            } else {
                                level.thingsAt(castX, castY).forEach {
                                    if (it.isAlwaysVisible()) resultSet[it] = distance
                                }
                            }
                            if (visible && level.isOpaqueAt(castX, castY)) {
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
