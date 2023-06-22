package world.path

import actors.Actor
import kotlinx.serialization.Serializable
import util.XY

@Serializable
class ActorStepMap : StepMap() {

    lateinit var targetID: String

    fun init(walker: Actor, range: Int, target: Actor) {
        this.targetID = target.id
        init(walker, range)
    }

    override fun onActorMove(actor: Actor) {
        super.onActorMove(actor)
        if (actor.id == targetID) {
            dirty = true
        }
    }

    override fun printTarget() {
        App.level.director.getActor(targetID)?.also { target ->
            writeTargetCell(target.xy.x, target.xy.y)
        }
    }

    override fun nextStep(from: Actor, to: Actor): XY? {
        if (from.id == walkerID && to.id == targetID) {
            return getNextStep(from.xy.x, from.xy.y)
        }
        return null
    }

    override fun nextStepAwayFrom(from: Actor, to: Actor): XY? {
        if (from.id == walkerID && to.id == targetID) {
            return getNextStepAway(from.xy.x, from.xy.y)
        }
        return null
    }
}
