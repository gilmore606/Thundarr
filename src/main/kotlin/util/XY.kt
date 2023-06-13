package util

import kotlinx.serialization.Serializable
import world.level.CHUNK_SIZE
import kotlin.math.pow
import kotlin.math.sqrt

// A mutable 2D vector suitable for passing coordinates.
@Serializable
data class XY(var x: Int, var y: Int) {
    operator fun plus(b: XY): XY {
        return XY(x + b.x, y + b.y)
    }
    operator fun minus(b: XY): XY {
        return XY(x - b.x, y - b.y)
    }
    operator fun times(b: Int): XY {
        return XY(x * b, y * b)
    }
    override fun equals(other: Any?): Boolean {
        return (other is XY) && (x == other.x && y == other.y)
    }

    fun distanceTo(bx: Int, by: Int): Float {
        val x0 = x.toFloat()
        val x1 = bx.toFloat()
        val y0 = y.toFloat()
        val y1 = by.toFloat()
        return sqrt((x1-x0).pow(2) + (y1-y0).pow(2))
    }
    fun distanceTo(b: XY) = distanceTo(b.x, b.y)

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
}

data class XYf(var x: Float, var y: Float) {
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