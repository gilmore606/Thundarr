package actors.statuses

import actors.actions.*
import actors.actors.Actor
import actors.actors.Player
import actors.stats.Speed
import actors.stats.skills.Dodge
import kotlinx.serialization.Serializable
import ui.panels.Console


@Serializable
class Stunned() : TimeStatus() {
    override val tag = Tag.STUNNED
    override fun name() = "stunned"
    override fun description() = "You're too stunned to move or attack."
    override fun onAddMsg() = "You're stunned!"
    override fun onAddOtherMsg() = "%Dn looks stunned!"
    override fun panelTag() = "stun"
    override fun panelTagColor() = tagColors[TagColor.FATAL]!!
    override fun statEffects() = mapOf(
        Speed.tag to -4f,
        Dodge.tag to -8f
    )
    override fun duration() = 1f
    override fun maxDuration() = 2f
    override fun preventActiveDefense() = true
    override fun preventedAction(action: Action, actor: Actor): Boolean {
        if (action is Move || action is WalkTo || action is Attack || action is Throw) {
            if (actor is Player) Console.say("You're too stunned to " + action.name() + "!")
            return true
        }
        return false
    }
}
