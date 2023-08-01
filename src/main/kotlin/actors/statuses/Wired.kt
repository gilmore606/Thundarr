package actors.statuses

import actors.stats.Speed
import actors.stats.skills.Fight
import kotlinx.serialization.Serializable


@Serializable
class Wired : TimeStatus() {
    override val tag = Tag.WIRED
    override fun name() = "wired"
    override fun description() = "Stimulant drugs drive your reflexes to extreme speed."
    override fun panelTag() = "wire"
    override fun panelTagColor() = tagColors[TagColor.GOOD]!!
    override fun onAddMsg() = "Your skin vibrates and your pupils dilate.  You feel speedy."
    override fun onAddOtherMsg() = "%Dn's movements speed up."
    override fun onRemoveMsg() = "You feel your nerves relax and slow back down."
    override fun onRemoveOtherMsg() = "%Dn's movements slow down to normal."
    override fun onStackMsg() = "Ahh...that should keep the party going."
    override fun statEffects() = mapOf(
        Speed.tag to 4f,
        Fight.tag to 1f
    )
    override fun duration() = 10f
    override fun maxDuration() = 20f
    override fun calorieBurnMod() = 1.2f
}
