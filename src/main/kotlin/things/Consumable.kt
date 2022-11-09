package things

import actors.Actor
import actors.Player
import actors.statuses.Status
import actors.statuses.Wired
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.panels.Console
import world.level.Level


@Serializable
sealed class Consumable : Portable() {
    open fun consumeVerb() = "eat"
    open fun consumeDuration() = 1f
    open fun consumeSelfMsg() = "You " + consumeVerb() + " all of %dd."
    open fun consumeOtherMsg() = "%Dn " + consumeVerb() + "s %in."

    override fun uses() = mapOf(
        UseTag.CONSUME to Use(consumeVerb() + " " + name(), consumeDuration(),
            canDo = { actor,x,y,targ -> !targ && isHeldBy(actor) && this.consumableBy(actor) },
            toDo = { actor, level, x, y ->
                Console.sayAct(consumeSelfMsg(), consumeOtherMsg(), actor, this)
                this.moveTo(null)
                onConsume(actor)
            })
    )

    open fun consumableBy(actor: Actor) = true

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

                    announced = true
                }
                if (!announced) Console.announce(level, xy.x, xy.y, Console.Reach.VISUAL, m + "!")
            }
        }
        moveTo(null)
    }

    override fun onThrownAt(level: Level, x: Int, y: Int) {
        super.onThrownAt(level, x, y)
        if (breakOnThrow()) {
            Console.announce(level, x, y, Console.Reach.VISUAL, dnamec() + "shatters.")
            moveTo(null)
        }
    }

    override fun onThrownOn(target: Actor) {
        if (breakOnThrow()) {
            Console.announce(
                target.level, target.xy.x, target.xy.y, Console.Reach.VISUAL,
                dnamec() + " shatters, splashing " + target.dname() + "!"
            )
            statusEffect()?.also { target.addStatus(it) }
            moveTo(null)
        } else {
            super.onThrownOn(target)
        }
    }
}

@Serializable
sealed class Food : Consumable() {
    override fun consumeDuration() = 2f
    override fun consumeSelfMsg() = "You wolf down %id."
    override fun consumableBy(actor: Actor) = (actor !is Player || actor.couldEat())
    open fun calories() = 200

    override fun onConsume(actor: Actor) {
        actor.ingestCalories(calories())
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
    override fun calories() = 800
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
