package util

import kotlinx.serialization.Serializable

@Serializable
data class Rect(var x0: Int, var y0: Int, var x1: Int, var y1: Int) {
    override fun toString() = "[$x0,$y0-$x1,$y1]"

    fun isTouching(other: Rect): Boolean {
        if (x0 > other.x1 + 1) return false
        if (x1 < other.x0 - 1) return false
        if (y0 > other.y1 + 1) return false
        if (y1 < other.y0 - 1) return false
        return true
    }

    fun contains(xy: XY): Boolean = (xy.x >= x0 && xy.y >= y0 && xy.x <= x1 && xy.y <= y1)
    fun isAdjacentTo(xy: XY): Boolean = (((xy.x == x0 - 1) || (xy.x == x1 + 1)) && (xy.y in y0 - 1 .. y1 + 1)) ||
            (((xy.y == y0 - 1) || (xy.y == y1 + 1)) && (xy.x in x0-1 .. x1 + 1))

    fun overlaps(rect: Rect): Boolean {
        if (rect.x0 > x1) return false
        if (rect.x1 < x0) return false
        if (rect.y0 > y1) return false
        if (rect.y1 < y0) return false
        return true
    }

    fun center(): XY = XY(
        x0 + (x1 - x0) / 2,
        y0 + (y1 - y0) / 2
    )

    fun width() = (x1 - x0) + 1
    fun height() = (y1 - y0) + 1
    fun area() = width() * height()

    fun randomPoint(): XY = XY(Dice.range(x0, x1), Dice.range(y0, y1))
}
