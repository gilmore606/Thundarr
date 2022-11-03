package world

class AttractLevel() : EnclosedLevel("attract") {

    companion object {
        val dimension = 120
    }

    override fun updateVisibility() {
        for (cx in 0 until width) {
            for (cy in 0 until height) {
                chunk?.setTileVisibility(cx, cy, true)
            }
        }
    }

}
