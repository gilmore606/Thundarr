package things

import actors.Actor
import actors.statuses.Status
import actors.statuses.Wired
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.input.Keyboard
import ui.panels.Console
import world.Level


@Serializable
sealed class Consumable : Portable() {
    open fun consumeVerb() = "eat"
    open fun consumeDuration() = 1f
    open fun consumeSelfMsg() = "You " + consumeVerb() + " all of %dd."
    open fun consumeOtherMsg() = "%Dn " + consumeVerb() + "s %in."

    override fun uses() = mapOf(
        UseTag.CONSUME to Use(consumeVerb() + " " + name(), consumeDuration(),
            canDo = { actor -> this in actor.contents },
            toDo = { actor, level ->
                Console.sayAct(consumeSelfMsg(), consumeOtherMsg(), actor, this)
                this.moveTo(null)
                onConsume(actor)
            })
    )

    override fun toolbarName() = consumeVerb() + " " + this.name()
    override fun toolbarUseTag() = UseTag.CONSUME

    open fun onConsume(actor: Actor) {
        statusEffect()?.also { actor.addStatus(it) }
    }

    open fun statusEffect(): Status? = null

    override fun description() = "Looks like you could eat it, if you were hungry enough.  Maybe you are."

    override fun examineInfo(): String {
        return statusEffect()?.let { effect ->
            consumeVerb().capitalize() + "ing this makes you " + effect.name() + "."
        } ?: super.examineInfo()
    }

    open fun breakOnThrow() = false
    open fun onBreak() {
        val m = dnamec() + " shatters"
        var announced = false
        level()?.also { level ->
            xy()?.also { xy ->
                level.actorAt(xy.x, xy.y)?.also { target ->
                    Console.announce(level, xy.x, xy.y, Console.Reach.VISUAL, m + ", splashing " + target.dname() + "!")
                    announced = true
                    statusEffect()?.also { target.addStatus(it) }
                }
                if (!announced) Console.announce(level, xy.x, xy.y, Console.Reach.VISUAL, m + "!")
            }
        }
        moveTo(null)
    }

    override fun onThrownAt(thrower: Actor, level: Level, x: Int, y: Int) {
        super.onThrownAt(thrower, level, x, y)
        if (breakOnThrow()) onBreak()
    }
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
}

@Serializable
class Pear : Food() {
    override fun glyph() = Glyph.FRUIT
    override fun hue() = 0.9f
    override fun name() = "pear"
}

@Serializable
class Meat : Food() {
    override fun glyph() = Glyph.MEAT
    override fun name() = "raw steak"
    override fun description() = "A bloody chunk of raw meat.  Your victim?  Sadly, this game does not keep track."
}

@Serializable
class EnergyDrink : Consumable() {
    override fun glyph() = Glyph.BOTTLE
    override fun name() = "energy drink"
    override fun consumeVerb() = "drink"
    override fun description() = "Taurine and caffeine to keep you active 24/7.  Or so it says on the can."
    override fun statusEffect() = Wired()
    override fun breakOnThrow() = true
}
