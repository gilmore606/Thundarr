package things

import actors.Actor
import actors.Player
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.panels.Console
import util.aOrAn


@Serializable
sealed class Consumable : Portable() {
    open fun consumeVerb() = "eat"
    open fun consumeDuration() = 1f
    open fun consumeSelfMsg() = "You " + consumeVerb() + " all of %dd."
    open fun consumeOtherMsg() = "%Dn " + consumeVerb() + "s %in."

    override fun uses() = setOf(
        Use(consumeVerb() + " " + name(), consumeDuration(),
            canDo = { actor -> this in actor.contents },
            toDo = { actor, level ->
                Console.sayAct(consumeSelfMsg(), consumeOtherMsg(), actor, this)
                this.moveTo(null)
                onConsume(actor)
            })
    )

    open fun onConsume(actor: Actor) { }

    override fun description() = "Looks like you could eat it, if you were hungry enough.  Maybe you are."
}

@Serializable
sealed class Food : Consumable() {
    override fun consumeDuration() = 2f
    override fun consumeSelfMsg() = "You wolf down %id.  That really hit the spot."

    open fun nutrition() = 5f

    override fun onConsume(actor: Actor) {
        actor.gainHealth(nutrition())
    }
}

@Serializable
class Apple : Food() {
    override fun glyph() = Glyph.FRUIT
    override fun name() = "apple"
    override val kind = Kind.APPLE
}

@Serializable
class Meat : Food() {
    override fun glyph() = Glyph.MEAT
    override fun name() = "raw steak"
    override val kind = Kind.MEAT
}

@Serializable
class EnergyDrink : Consumable() {
    override fun glyph() = Glyph.BOTTLE
    override fun name() = "energy drink"
    override val kind = Kind.ENERGY_DRINK

    override fun consumeVerb() = "drink"

    override fun description() = "Taurine and caffeine to keep you active 24/7.  Or so it says on the can."
}
