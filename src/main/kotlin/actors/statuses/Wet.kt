package actors.statuses

import actors.stats.Speed
import kotlinx.serialization.Serializable


@Serializable
class Wet(): TimeStatus() {
    var wetness = 1f
    override val tag = Tag.WET
    override fun name() = "wet"
    override fun description() = "You're soaking wet."
    override fun panelTag() = "wet"
    override fun panelTagColor() = tagColors[TagColor.NORMAL]!!
    override fun statEffects() = mapOf(
        Speed.tag to -1f
    )
    override fun comfort() = -0.5f
    override fun duration() = 10f
    override fun maxDuration() = 40f
    fun temperatureMod() = (wetness * (turnsLeft / maxDuration()) * -28).toInt()
    fun addWetness(added: Float) {
        wetness = java.lang.Float.min(1f, wetness + added)
        addTime += duration() * added * 2f
        turnsLeft = java.lang.Float.min(maxDuration(), turnsLeft + wetness * duration())
    }
}
