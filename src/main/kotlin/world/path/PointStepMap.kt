package world.path

import actors.Actor
import kotlinx.serialization.Serializable
import util.XY
import util.log

@Serializable
class PointStepMap : StepMap() {

    lateinit var target: XY

    fun init(walker: Actor, range: Int, target: XY) {
        init(walker, range)
        this.target = target
    }

    override fun toString() = "PointStepMap(from $walker to $target)"

    override fun printTarget() {
        writeTargetCell(target.x, target.y)
    }

    override fun nextStep(from: Actor, to: XY): XY? {
        if (from.id == walkerID  && to == target) {
            return getNextStep(from.xy().x, from.xy().y)
        }
        return null
    }

}
