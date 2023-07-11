package actors.stats.skills

import actors.stats.Senses
import actors.stats.Speed
import actors.stats.Strength

object Bows : Skill(Tag.BOWS, "bows", setOf(Strength, Senses)) {
    override fun description() = "Accurately shooting bows and crossbows."
    override fun verb() = "archery"
}
