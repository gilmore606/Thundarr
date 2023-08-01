package actors.statuses

import actors.actions.Action
import actors.actions.Use
import actors.actors.Actor
import actors.actors.Player
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import kotlinx.serialization.Serializable
import things.Thing
import ui.panels.Console
import util.Dice


@Serializable
class Satiated() : Status() {
    override val tag = Tag.SATIATED
    override fun name() = "satiated"
    override fun description() = "Couldn't eat another bite, really."
    override fun panelTag() = "sat"
    override fun panelTagColor() = tagColors[TagColor.GOOD]!!
    override fun onAddMsg() = "Oof, you're full."
    override fun statEffects() = mapOf(
        Speed.tag to -1f
    )
    override fun comfort() = 0.2f
    override fun calorieBurnMod() = 0.7f

    override fun preventedAction(action: Action, actor: Actor): Boolean {
        if (action is Use && action.useTag == Thing.UseTag.CONSUME) {
            if (actor is Player) Console.say("You feel too full to eat or drink anything.")
            return true
        }
        return false
    }
}

@Serializable
class Hungry() : Status() {
    override val tag = Tag.HUNGRY
    override fun name() = "hungry"
    override fun description() = "Lack of calories is making you weak and foggy."
    override fun panelTag() = "hung"
    override fun panelTagColor() = tagColors[TagColor.BAD]!!
    override fun statEffects() = mapOf(
        Strength.tag to -1f,
        Brains.tag to -1f,
        Speed.tag to -1f
    )
    override fun comfort() = -0.5f
    override fun calorieBurnMod() = 0.8f
}

@Serializable
class Starving() : Status() {
    override val tag = Tag.STARVING
    override fun name() = "starving"
    override fun description() = "Lack of calories is taking its toll.  You'll die soon without food."
    override fun panelTag() = "starv"
    override fun panelTagColor() = tagColors[TagColor.FATAL]!!
    override fun statEffects() = mapOf(
        Strength.tag to -2f,
        Brains.tag to -2f,
        Speed.tag to -2f
    )
    override fun comfort() = -1f
    override fun calorieBurnMod() = 0.7f
    override fun advanceTime(actor: Actor, delta: Float) {
        if (Dice.chance(0.02f * delta)) {
            Console.say("You're starving to death!")
            actor.receiveDamage(1f, internal = true)
        }
    }
}
