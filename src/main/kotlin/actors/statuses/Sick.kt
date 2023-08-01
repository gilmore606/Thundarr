package actors.statuses

import actors.actions.Action
import actors.actions.Use
import actors.actors.Actor
import actors.actors.Player
import actors.stats.Brains
import actors.stats.Strength
import kotlinx.serialization.Serializable
import things.Thing
import ui.panels.Console
import util.Dice


@Serializable
class Sick(): TimeStatus() {
    override val tag = Tag.SICK
    override fun name() = "sick"
    override fun description() = "You're too nauseous to eat."
    override fun onAddMsg() = "Ugh, you must have eaten something bad.  You feel sick."
    override fun onRemoveMsg() = "Your stomach feels better.  You could probably eat something."
    override fun panelTag() = "sick"
    override fun panelTagColor() = tagColors[TagColor.BAD]!!
    override fun statEffects() = mapOf(
        Strength.tag to -1f,
        Brains.tag to -1f
    )
    override fun comfort() = -0.3f
    override fun calorieBurnMod() = 1.2f
    override fun duration() = Dice.float(40f, 80f)
    override fun maxDuration() = 300f
    override fun preventedAction(action: Action, actor: Actor): Boolean {
        if (action is Use && action.useTag == Thing.UseTag.CONSUME) {
            if (actor is Player) Console.say("You feel too nauseous to eat or drink anything.")
            return true
        }
        return false
    }
}
