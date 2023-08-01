package actors.statuses

import actors.stats.Brains
import actors.stats.Speed
import kotlinx.serialization.Serializable


@Serializable
class Dazed : TimeStatus() {
    override val tag = Tag.DAZED
    override fun name() = "dazed"
    override fun description() = "A minor concussion makes it hard to think or move for a short time."
    override fun panelTag() = "daze"
    override fun panelTagColor() = tagColors[TagColor.BAD]!!
    override fun onAddMsg() = "You stagger, dazed."
    override fun onAddOtherMsg()  = "%Dn staggers!"
    override fun onRemoveMsg() = "You shake out of your daze."
    override fun onRemoveOtherMsg() = "%Dn shakes out of %p daze."
    override fun onStackMsg() = "Whooaaa!"
    override fun statEffects() = mapOf(
        Speed.tag to -2f,
        Brains.tag to -4f
    )
    override fun duration() = 3f
    override fun maxDuration() = 5f
}
