package actors.statuses

import actors.actions.Action
import actors.actions.Attack
import actors.actions.Move
import actors.actors.Actor
import actors.actors.Player
import actors.stats.Heart
import kotlinx.serialization.Serializable
import ui.panels.Console
import util.distanceBetween

@Serializable
class Afraid(
    val targetId: String,
    val targetName: String,
) : Status() {
    override val tag = Tag.AFRAID
    override fun name() = "afraid"
    override fun description() = "You're terrified of ${targetName}!"
    override fun onAddMsg() = "You're overcome with fear!"
    override fun onAddOtherMsg() = "%Dn cowers in fear!"
    override fun panelTag() = "fear"
    override fun panelTagColor() = tagColors[TagColor.BAD]!!

    override fun advanceTime(actor: Actor, delta: Float) {
        actor.level?.director?.getActor(targetId)?.also { target ->
            if (!actor.canSee(target)) done = true
        } ?: run { done = true }

        if (Heart.resolve(actor, -1f) >= 0) {
            if (actor is Player) Console.say("You gather your courage.")
            done = true
        }
    }

    override fun preventedAction(action: Action, actor: Actor): Boolean {
        actor.level?.director?.getActor(targetId)?.also { target ->
            if (action is Move) {
                if (distanceBetween(actor.xy + action.dir, target.xy) < distanceBetween(actor.xy, target.xy)) {
                    if (actor is Player) Console.say("You're too afraid to approach ${target.dname()}!")
                    return true
                }
            }
            if (action is Attack) {
                if (actor is Player) Console.say("You're too afraid to attack ${target.dname()}!")
                return true
            }
        }
        return false
    }
}
