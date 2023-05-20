package things

import actors.Actor
import actors.Player
import actors.statuses.Sick
import actors.statuses.Status
import actors.statuses.Wired
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.panels.Console
import util.Dice
import world.level.Level


@Serializable
sealed class Consumable : Portable() {
    open fun consumeVerb() = "eat"
    open fun consumeDuration() = 1f
    open fun consumeSelfMsg() = "You " + consumeVerb() + " all of %dd."
    open fun consumeOtherMsg() = "%Dn " + consumeVerb() + "s %in."

    override fun category() = Category.CONSUMABLE

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

    override fun spawnContainers() = mutableListOf(Tag.THING_FRIDGE, Tag.THING_TABLE)

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
            xy().also { xy ->
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
    override fun consumableBy(actor: Actor) = true
    override fun weight() = 0.5f
    open fun calories() = 200

    override fun onConsume(actor: Actor) {
        actor.ingestCalories(calories())
    }
}

@Serializable
class Apple : Food() {
    override val tag = Tag.THING_APPLE
    override fun glyph() = Glyph.FRUIT
    override fun name() = "apple"
    override fun weight() = 0.1f
}

@Serializable
class Pear : Food() {
    override val tag = Tag.THING_PEAR
    override fun glyph() = Glyph.FRUIT
    override fun hue() = 0.9f
    override fun name() = "pear"
    override fun weight() = 0.1f
}

@Serializable
class WizardcapMushroom : Food() {
    override val tag = Tag.THING_WIZARDCAP_MUSHROOM
    override fun name() = "wizardcap mushroom"
    override fun glyph() = Glyph.MUSHROOM
    override fun weight() = 0.05f
}

@Serializable
class SpeckledMushroom : Food() {
    override val tag = Tag.THING_SPECKLED_MUSHROOM
    override fun name() = "speckled mushroom"
    override fun glyph() = Glyph.TOADSTOOLS
    override fun weight() = 0.05f
}

@Serializable
class BloodcapMushroom : Food() {
    override val tag = Tag.THING_BLOODCAP_MUSHROOM
    override fun name() = "bloodcap mushroom"
    override fun glyph() = Glyph.TOADSTOOLS
    override fun hue() = 0.5f
    override fun weight() = 0.05f
}

@Serializable
class RawMeat : Rottable() {
    override val tag = Tag.THING_RAWMEAT
    override fun glyph() = Glyph.MEAT
    override fun name() = "raw meat"
    override fun description() = "A bloody chunk of raw meat.  Edible as-is, but not exactly appetizing; you're a barbarian, not a savage."
    override fun calories() = 600
    override fun consumeSelfMsg() = "You choke down the raw meat, imagining how delicious it would be if you cooked it first.  Oh well."
    override fun onConsume(actor: Actor) {
        super.onConsume(actor)
        if (Dice.chance(0.3f)) {
            actor.addStatus(Sick())
        }
    }
}

@Serializable
class Steak : Food() {
    override val tag = Tag.THING_STEAK
    override fun glyph() = Glyph.MEAT
    override fun hue() = 0.5f
    override fun name() = "seared steak"
    override fun description() = "A delicious flame-grilled slab of steak.  From what animal, is not important.  It smells wonderful."
    override fun calories() = 1200
    override fun consumeSelfMsg() = "You feast on the steak, congratulating yourself for your domestic prowess."
}

@Serializable
class ChickenLeg : Food() {
    override val tag = Tag.THING_CHICKENLEG
    override fun glyph() = Glyph.CHICKEN_LEG
    override fun name() = "chicken leg"
    override fun description() = "You're not sure it actually came from a chicken, but it's cooked and smells tasty."
    override fun calories() = 1000
    override fun consumeSelfMsg() = "You hungrily strip the meat from the bird leg."
}

@Serializable
class Cheese : Food() {
    override val tag = Tag.THING_CHEESE
    override fun glyph() = Glyph.CHEESE
    override fun name() = "cheese"
    override fun description() = "A wedge of hard cheese.  It smells like Ookla."
    override fun calories() = 1000
    override fun consumeSelfMsg() = "You munch on the salty cheese."
}

@Serializable
class Stew : Food() {
    override val tag = Tag.THING_STEW
    override fun glyph() = Glyph.STEW
    override fun name() = "stew"
    override fun description() = "A nutritious melange of meat and vegetables."
    override fun calories() = 1600
    override fun consumeSelfMsg() = "You wolf down every last bite of the tasty stew."
}

@Serializable
class ThrallChow : Food() {
    override val tag = Tag.THING_THRALLCHOW
    override fun glyph() = Glyph.PACKAGE_BAG
    override fun name() = "thrall-chow"
    override fun description() = "A plastic bag of Thrall-Chow(tm).  'For minimum Thrall sustenance,' says the colorful package."
    override fun calories() = 1200
    override fun consumeSelfMsg() = "You choke down the dry fibrous chow chips.  How depressing."
}

@Serializable
class EnergyDrink : Consumable() {
    override val tag = Tag.THING_ENERGYDRINK
    override fun glyph() = Glyph.BOTTLE
    override fun name() = "energy drink"
    override fun consumeVerb() = "drink"
    override fun description() = "Taurine and caffeine to keep you active 24/7.  Or so it says on the can."
    override fun statusEffect() = Wired()
    override fun breakOnThrow() = true
    override fun weight() = 0.1f
}

@Serializable
sealed class Rottable : Food(), Temporal {
    private var rot = 0f
    open fun maxRot() = 2000f
    open fun onRot() { }

    override fun examineDescription(): String {
        var d = super.examineDescription()
        val max = maxRot()
        if (rot > max * 0.75f) {
            d += " It smells pretty bad."
        } else if (rot > max * 0.5f) {
            d += " It smells a little off."
        }
        return d
    }

    override fun advanceTime(delta: Float) {
        if (holder is Fridge && (holder as Fridge).isRefrigerating()) return
        rot += delta
        if (rot > maxRot()) {
            onRot()
            doRot()
        }
    }
    private fun doRot() {
        if (holder is Player) Console.say("Your " + this.name() + " rots away.")
        moveTo(null)
    }
}
