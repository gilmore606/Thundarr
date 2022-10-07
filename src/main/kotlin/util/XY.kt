package util

import kotlin.math.pow
import kotlin.math.sqrt

// A mutable 2D vector suitable for passing coordinates.
class XY(var x: Int, var y: Int) {
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

    fun distanceTo(b: XY): Float {
        val x0 = x.toFloat()
        val x1 = b.x.toFloat()
        val y0 = y.toFloat()
        val y1 = b.y.toFloat()
        return sqrt((x1-x0).pow(2) + (y1-y0).pow(2))
    }
}
