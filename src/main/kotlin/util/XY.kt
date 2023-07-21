package util

import kotlinx.serialization.Serializable
import world.Entity
import world.level.CHUNK_SIZE
import kotlin.math.pow
import kotlin.math.sqrt

// A mutable 2D vector suitable for passing coordinates.
@Serializable
data class XY(var x: Int, var y: Int) {
    override fun toString() = "$x,$y"

    operator fun plus(b: XY): XY {
        return XY(x + b.x, y + b.y)
    }
    operator fun plus(b: XYd): XYd {
        return XYd(x + b.x, y + b.y)
    }
    operator fun minus(b: XY): XY {
        return XY(x - b.x, y - b.y)
    }
    operator fun times(b: Int): XY {
        return XY(x * b, y * b)
    }
    operator fun times(b: Double): XY {
        return XY((x * b).toInt(), (y * b).toInt())
    }
    operator fun div(b: Float): XYf {
        return XYf(x / b, y / b)
    }

    override fun equals(other: Any?): Boolean {
        return (other is XY) && (x == other.x && y == other.y)
    }

    fun add(xy: XY) {
        x += xy.x
        y += xy.y
    }

    fun scaleBy(b: Float) {
        x = (x * b).toInt()
        y = (y * b).toInt()
    }

    fun setTo(entity: Entity) {
        x = entity.xy().x
        y = entity.xy().y
    }

    fun setTo(newXY: XY) {
        x = newXY.x
        y = newXY.y
    }

    fun moveToInt(newXY: XYd) {
        x = newXY.x.toInt()
        y = newXY.y.toInt()
    }

    fun setTo(newX: Int, newY: Int) {
        x = newX
        y = newY
    }

    fun distanceTo(bx: Int, by: Int): Float {
        val x0 = x.toFloat()
        val x1 = bx.toFloat()
        val y0 = y.toFloat()
        val y1 = by.toFloat()
        return sqrt((x1-x0).pow(2) + (y1-y0).pow(2))
    }
    fun distanceTo(b: XY) = distanceTo(b.x, b.y)

    fun isAdjacentTo(b: XY): Boolean = (b.x in x-1..x+1 && b.y in y-1..y+1) && (b != this)

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

    fun toXYf() = XYf(x.toFloat(), y.toFloat())

    fun rotated() = XY(y, x)
    fun flipped() = XY(-x, -y)

    fun isFarEnoughFromAll(dist: Int, all: List<XY>): Boolean {
        all.forEach { other ->
            if (distanceTo(other.x, other.y) < dist) return false
        }
        return true
    }

    fun dirPlusDiagonals(): List<XY> {
        val i = DIRECTIONS_ROSE.indexOf(this)
        val i2 = (if (i == 0) DIRECTIONS_ROSE.lastIndex else i - 1)
        val i3 = (if (i == DIRECTIONS_ROSE.lastIndex) 0 else i + 1)
        return listOf(this, DIRECTIONS_ROSE[i2], DIRECTIONS_ROSE[i3])
    }

    fun describeDirectionTo(to: XY): String {
        val xd = to.x - x
        val yd = to.y - y
        return if (Math.abs(xd) > Math.abs(yd * 2)) {
            if (xd > 0) "east" else "west"
        } else if (Math.abs(yd) > Math.abs(xd * 2)) {
            if (yd > 0) "south" else "north"
        } else if (xd < 0 && yd < 0) "northwest"
        else if (xd > 0 && yd > 0) "southeast"
        else if (xd > 0 && yd < 0) "northeast"
        else "southwest"
    }
}

data class XYf(var x: Float, var y: Float) {
    override fun toString() = "$x,$y"

    operator fun plus(b: XYf): XYf {
        return XYf(x + b.x, y + b.y)
    }
    operator fun minus(b: XYf): XYf {
        return XYf(x - b.x, y - b.y)
    }
    operator fun times(b: Float): XYf {
        return XYf(x * b, y * b)
    }
    override fun equals(other: Any?): Boolean {
        return (other is XYf) && (x == other.x && y == other.y)
    }
}

data class XYd(var x: Double, var y: Double) {
    override fun toString() = "$x,$y"

    operator fun plus(b: XYd): XYd {
        return XYd(x + b.x, y + b.y)
    }
    operator fun plus(b: XY): XYd {
        return XYd(x + b.x, y + b.y)
    }
    operator fun minus(b: XYd): XYd {
        return XYd(x - b.x, y - b.y)
    }
    operator fun minus(b: XY): XYd {
        return XYd(x - b.x, y - b.y)
    }
    operator fun times(b: Double): XYd {
        return XYd(x * b, y * b)
    }
    operator fun times(b: Float): XYd {
        return XYd(x * b, y * b)
    }
    operator fun div(b: Double): XYd {
        return XYd(x / b, y / b)
    }
    override fun equals(other: Any?): Boolean {
        return (other is XYd) && (x == other.x && y == other.y)
    }

    fun add(xy: XY) {
        x += xy.x
        y += xy.y
    }
    fun add(xy: XYf) {
        x += xy.x
        y += xy.y
    }
    fun add(xy: XYd) {
        x += xy.x
        y += xy.y
    }
    fun add(bx: Double, by: Double) {
        x += bx
        y += by
    }
    fun setTo(newXY: XYd) {
        x = newXY.x
        y = newXY.y
    }
    fun setTo(newXY: XY) {
        x = newXY.x.toDouble()
        y = newXY.y.toDouble()
    }

    fun setTo(newX: Double, newY: Double) {
        x = newX
        y = newY
    }
    fun magnitude() = sqrt(x.pow(2) + y.pow(2))
    fun toUnitVec(): XYd {
        val mag = magnitude()
        if (mag < 1.0) return this
        return XYd(x / mag, y / mag)
    }
}

fun dirToEdge(dir: XY, offset: Int = 0): XY = when (dir) {
    NORTH -> XY(CHUNK_SIZE / 2 + offset, 0)
    SOUTH -> XY(CHUNK_SIZE / 2 + offset, CHUNK_SIZE - 1)
    WEST -> XY(0, CHUNK_SIZE / 2 + offset)
    EAST -> XY(CHUNK_SIZE - 1, CHUNK_SIZE / 2 + offset)
    NORTHEAST -> XY(CHUNK_SIZE - 1, 0)
    SOUTHEAST -> XY(CHUNK_SIZE - 1, CHUNK_SIZE - 1)
    NORTHWEST -> XY(0, 0)
    SOUTHWEST -> XY(0, CHUNK_SIZE - 1)
    else -> XY(0,0)
}