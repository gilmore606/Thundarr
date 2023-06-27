package actors.stats.skills

import actors.stats.Brains
import actors.stats.Senses

object Sneak : Skill(Tag.SNEAK, "sneak", setOf(Brains, Senses)) {
    override fun description() = "Avoiding notice."
    override fun verb() = "sneak"
}
