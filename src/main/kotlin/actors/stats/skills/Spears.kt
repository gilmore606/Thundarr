package actors.stats.skills

import actors.stats.Senses
import actors.stats.Speed
import actors.stats.Strength

object Spears : Skill(Tag.SPEARS, "spears", setOf(Speed, Senses)) {
    override fun description() = "Fighting with spears and other polearms."
    override fun verb() = "spear fighting"
}
