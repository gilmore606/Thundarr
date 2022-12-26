package util

class Evolver(
    val width: Int,
    val height: Int,
    val cardinalsOnly: Boolean,
    val readCell: (x: Int, y: Int)->Boolean,
    val writeCell: (x: Int, y: Int)->Unit,
    val rule: (n: Int)->Boolean
) {
    fun evolve(repeats: Int = 1) {
        val adds = ArrayList<XY>()
        repeat(repeats) {
            for (ix in 0 until width) {
                for (iy in 0 until height) {
                    var n = 0
                    (if (cardinalsOnly) CARDINALS else DIRECTIONS).forEach { dir ->
                        val dx = ix + dir.x
                        val dy = iy + dir.y
                        if (readCell(dx, dy)) n++
                    }
                    if (rule(n)) {
                        adds.add(XY(ix, iy))
                    }
                }
            }
            adds.forEach { writeCell(it.x, it.y) }
            adds.clear()
        }
    }
}
