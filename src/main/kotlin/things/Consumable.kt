package things

import actors.Actor
import actors.Player
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.panels.ConsolePanel
import util.aOrAn


@Serializable
sealed class Consumable : Portable() {
    open fun consumeVerb() = "eat"
    open fun consumeDuration() = 1f
    open fun consumeSelfMsg() = "You " + consumeVerb() + " all of the " + name() + "."
    open fun consumeOtherMsg(actor: Actor) = "Some guy " + consumeVerb() + " " + name().aOrAn() + "."

    override fun uses() = setOf(
        Use(consumeVerb() + " " + name(), consumeDuration(),
            canDo = { actor -> this in actor.contents },
            toDo = { actor, level ->
                ConsolePanel.say(if (actor is Player) consumeSelfMsg() else consumeOtherMsg(actor) )
                this.moveTo(null)
                onConsume(actor)
            })
    )

    open fun onConsume(actor: Actor) { }

    override fun description() = "Looks like you could eat it, if you were hungry enough.  Maybe you are."
}

@Serializable
class Apple : Consumable() {
    override fun glyph() = Glyph.FRUIT
    override fun name() = "apple"
    override val kind = Kind.APPLE
}

@Serializable
class EnergyDrink : Consumable() {
    override fun glyph() = Glyph.BOTTLE
    override fun name() = "energy drink"
    override val kind = Kind.ENERGY_DRINK

    override fun consumeVerb() = "drink"

    override fun description() = "Taurine and caffeine to keep you active 24/7.  Or so it says on the can."
}
