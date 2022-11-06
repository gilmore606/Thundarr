package actors.stats.skills

import actors.stats.Speed

object Dodge : Skill(Tag.DODGE, "dodge", setOf(Speed)) {
    override fun description() = "Getting out of the way of trouble."
    override fun verb() = "dodging"
}
