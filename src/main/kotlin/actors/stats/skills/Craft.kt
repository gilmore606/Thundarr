package actors.stats.skills

import actors.stats.Brains
import actors.stats.Senses

object Craft : Skill(Tag.CRAFT, "craft", setOf(Brains, Senses)) {
    override fun description() = "Crafting useful things from raw materials."
    override fun verb() = "crafting"
}
