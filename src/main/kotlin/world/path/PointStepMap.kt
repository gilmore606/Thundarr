package world.path

import actors.Actor
import kotlinx.serialization.Serializable
import util.XY

@Serializable
data class PointStepMap(
    val target: XY,
) : StepMap() {

    override fun toString() = "PointStepMap(from $walker to $target)"
    override fun getClone(): StepMap = this.copy()

    override fun printTarget() {
        writeTargetCell(target.x, target.y)
    }

    override fun nextStep(from: Actor, to: XY): XY? {
        if (from.id == walkerID  && to == target) {
            return getNextStep(from, from.xy().x, from.xy().y)
        }
        return null
    }

}
