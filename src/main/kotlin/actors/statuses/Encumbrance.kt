package actors.statuses

import actors.stats.Speed
import actors.stats.skills.Dodge
import kotlinx.serialization.Serializable


@Serializable
class Encumbered() : Status() {
    override val tag = Tag.ENCUMBERED
    override fun name() = "encumbered"
    override fun description() = "All your stuff is weighing you down, making you slow."
    override fun panelTag() = "enc"
    override fun panelTagColor() = tagColors[TagColor.BAD]!!
    override fun statEffects() = mapOf(
        Speed.tag to -2f,
        Dodge.tag to -2f
    )
    override fun calorieBurnMod() = 1.2f
}

@Serializable
class Burdened() : Status() {
    override val tag = Tag.BURDENED
    override fun name() = "burdened"
    override fun description() = "You're carrying as much as you can, making you very slow."
    override fun panelTag() = "burd"
    override fun panelTagColor() = tagColors[TagColor.FATAL]!!
    override fun statEffects() = mapOf(
        Speed.tag to -4f,
        Dodge.tag to -3f
    )
    override fun calorieBurnMod() = 1.4f
}
