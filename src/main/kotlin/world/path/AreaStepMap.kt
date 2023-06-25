package world.path

import actors.Actor
import kotlinx.serialization.Serializable
import util.Rect
import util.XY

@Serializable
data class AreaStepMap(
    val target: Rect,
) : StepMap() {

    override fun toString() = "AreaStepMap(from $walker to $target)"
    override fun getClone(): StepMap = this.copy()

    override fun printTarget() {
        for (ix in target.x0..target.x1) {
            for (iy in target.y0..target.y1) {
                writeTargetCell(ix, iy)
            }
        }
    }

    override fun nextStep(from: Actor, to: Rect): XY? {
        if (from.id == walkerID && to == target) {
            return getNextStep(from, from.xy().x, from.xy().y)
        }
        return null
    }
}
