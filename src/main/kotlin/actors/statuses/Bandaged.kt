package actors.statuses

import actors.actors.Actor
import kotlinx.serialization.Serializable
import world.journal.GameTime

@Serializable
class Bandaged(
    val quality: Float
) : TimeStatus() {
    override val tag = Tag.BANDAGED
    override fun name() = "bandaged"
    override fun description() = "Your wounds are bound and cleaned to assist healing."
    override fun panelTag() = "band"
    override fun panelTagColor() = tagColors[TagColor.GOOD]!!
    override fun duration() = 300f + quality * 15f
    override fun maxDuration() = 800f
    override fun advanceTime(actor: Actor, delta: Float) {
        super.advanceTime(actor, delta)
        val healPerDay = 16f + quality * 2f
        actor.healDamage((delta / GameTime.TURNS_PER_DAY).toFloat() * healPerDay)
    }
    override fun comfort() = 0.3f
    override fun calorieBurnMod() = 1.4f
}
