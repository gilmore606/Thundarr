package actors.statuses

import actors.actors.Actor
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import kotlinx.serialization.Serializable
import ui.panels.Console
import util.Dice

@Serializable
class Cold(): Status() {
    companion object {
        const val threshold = 40
    }
    override val tag = Tag.COLD
    override fun name() = "cold"
    override fun description() = "You're shivering with cold."
    override fun panelTag() = "cold"
    override fun panelTagColor() = tagColors[TagColor.BAD]!!
    override fun statEffects() = mapOf(
        Strength.tag to -1f,
        Brains.tag to -2f,
        Speed.tag to -2f
    )
    override fun comfort() = -0.5f
    override fun calorieBurnMod() = 1.3f
}

@Serializable
class Freezing(): Status() {
    companion object {
        const val threshold = 20
    }
    override val tag = Tag.FREEZING
    override fun name() = "freezing"
    override fun description() = "You're freezing to death."
    override fun panelTag() = "frz"
    override fun panelTagColor() = tagColors[TagColor.FATAL]!!
    override fun statEffects() = mapOf(
        Strength.tag to -2f,
        Brains.tag to -3f,
        Speed.tag to -4f
    )
    override fun comfort() = -1f
    override fun calorieBurnMod() = 2f
    override fun advanceTime(actor: Actor, delta: Float) {
        if (Dice.chance(0.1f * delta)) {
            Console.say("You're freezing to death!")
            actor.receiveDamage(Dice.float(0.5f, 2.5f), internal = true)
        }
    }
}

@Serializable
class Hot(): Status() {
    companion object {
        const val threshold = 95
    }
    override val tag = Tag.HOT
    override fun name() = "hot"
    override fun description() = "You're dizzy and sweating."
    override fun panelTag() = "hot"
    override fun panelTagColor() = tagColors[TagColor.BAD]!!
    override fun statEffects() = mapOf(
        Strength.tag to -1f,
        Brains.tag to -2f,
        Speed.tag to -2f
    )
    override fun comfort() = -0.2f
}

@Serializable
class Heatstroke(): Status() {
    companion object {
        const val threshold = 115
    }
    override val tag = Tag.HEATSTROKE
    override fun name() = "heat"
    override fun description() = "You're slowly dying of heatstroke."
    override fun panelTag() = "heat"
    override fun panelTagColor() = tagColors[TagColor.FATAL]!!
    override fun statEffects() = mapOf(
        Strength.tag to -2f,
        Brains.tag to -3f,
        Speed.tag to -2f
    )
    override fun comfort() = -0.7f
    override fun advanceTime(actor: Actor, delta: Float) {
        if (Dice.chance(0.04f * delta)) {
            Console.say("You're dying of heatstroke!")
            actor.receiveDamage(1f,  internal = true)
        }
    }
}
