package path

import actors.actors.Actor
import kotlinx.serialization.Serializable
import util.XY

@Serializable
data class ActorStepMap(
    val targetID: String,
) : StepMap() {
    override fun toString() = "ActorStepMap(from $walker to $targetID)"
    override fun getClone(): StepMap = prepareCopy(this.copy())

    override fun onActorMove(actor: Actor) {
        super.onActorMove(actor)
        if (actor.id == targetID) {
            dirty = true
        }
    }

    override fun printTarget() {
        // TODO: fix concurrent mod error here (getActor crosses threads)
        App.level.director.getActor(targetID)?.also { target ->
            writeTargetCell(target.xy.x, target.xy.y)
        }
    }

    override fun nextStep(from: Actor, to: Actor): XY? {
        if (from.id == walkerID && to.id == targetID) {
            return getNextStep(from, from.xy.x, from.xy.y)
        }
        return null
    }

    override fun nextStepAwayFrom(from: Actor, to: Actor): XY? {
        if (from.id == walkerID && to.id == targetID) {
            return getNextStepAway(from, from.xy.x, from.xy.y)
        }
        return null
    }
}
