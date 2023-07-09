package world.path

import actors.Actor
import kotlinx.serialization.Serializable
import util.Rect
import util.XY
import util.forXY

@Serializable
data class AreaStepMap(
    val target: Rect,
) : StepMap() {

    override fun toString() = "AreaStepMap(from $walker to $target)"
    override fun getClone(): StepMap = this.copy()

    override fun printTarget() {
        forXY(target) { ix,iy ->
            writeTargetCell(ix, iy)
        }
    }

    override fun nextStep(from: Actor, to: Rect): XY? {
        if (from.id == walkerID && to == target) {
            return getNextStep(from, from.xy().x, from.xy().y)
        }
        return null
    }
}
