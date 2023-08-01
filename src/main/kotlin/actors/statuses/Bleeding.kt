package actors.statuses

import actors.actors.Actor
import kotlinx.serialization.Serializable
import util.Dice


@Serializable
class Bleeding(
    val hpPerTick: Float
) : TimeStatus() {
    override val tag = Tag.BLEEDING
    override fun name() = "bleeding"
    override fun description() = "You're losing blood fast!"
    override fun onAddMsg() = "You're bleeding out!"
    override fun onAddOtherMsg() = "%Dn's wounds gush blood."
    override fun panelTag() = "bleed"
    override fun panelTagColor() = tagColors[TagColor.FATAL]!!
    override fun duration() = 6f + Dice.float(0f, hpPerTick) * 4f
    override fun maxDuration() = 20f
    override fun advanceTime(actor: Actor, delta: Float) {
        super.advanceTime(actor, delta)
        actor.receiveDamage(hpPerTick * delta, internal = true)
    }
}
