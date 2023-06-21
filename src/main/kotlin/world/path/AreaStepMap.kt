package world.path

import actors.Actor
import kotlinx.serialization.Serializable
import util.DIRECTIONS
import util.Rect
import util.XY
import util.from

@Serializable
class AreaStepMap : StepMap() {

    lateinit var target: Rect

    fun init(walker: Actor, range: Int, target: Rect) {
        init(walker, range)
        this.target = target
    }

    override fun toString() = "AreaStepMap(from $walker to $target)"

    override fun printTarget() {
        for (ix in target.x0..target.x1) {
            for (iy in target.y0..target.y1) {
                writeTargetCell(ix, iy)
            }
        }
    }

    override fun nextStep(from: Actor, to: Rect): XY? {
        if (from.id == walkerID && to == target) {
            return getNextStep(from.xy().x, from.xy().y)
        }
        return null
    }
}
