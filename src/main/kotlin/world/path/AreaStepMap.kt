package world.path

import util.*
import world.Entity
import world.level.Level

class AreaStepMap : StepMap() {

    var targetRect: Rect? = null

    fun setTargetToRect(newTarget: Rect, level: Level) {
        this.level = level
        targetRect = newTarget
        offsetX = newTarget.x0 + newTarget.width() / 2 - width / 2
        offsetY = newTarget.y0 + newTarget.height() / 2 - height / 2
        outOfDate = true
    }

    override fun toString() = "AreaStepMap(target=$targetRect)"

    override suspend fun update() {
        targetRect?.also { target ->
            clearScratch()
            for (ix in target.x0..target.x1) {
                for (iy in target.y0..target.y1) {
                    scratch[ix - offsetX][iy - offsetY] = 0
                }
            }
            super.update()
        }
    }

    override fun canReach(to: Rect) = to == targetRect

    override fun nextStep(from: Entity, to: Rect): XY? {
        val fromX = from.xy().x
        val fromY = from.xy().y
        if (to == targetRect) {
            val lx = fromX - offsetX
            val ly = fromY - offsetY
            if (lx in 0 until width && ly in 0 until height) {
                val nextstep = map[lx][ly] - 1
                if (nextstep < 0) return null
                var step: XY? = null
                DIRECTIONS.from(lx, ly) { tx, ty, dir ->
                    if (tx in 0 until width && ty in 0 until height) {
                        if (map[tx][ty] == nextstep) {
                            step = dir
                        }
                    }
                }
                step?.also { return it }
            }
        }
        return null
    }
}
