package actors.stats.skills

import actors.stats.Speed
import actors.stats.Strength

object Blades : Skill(Tag.SWORDS, "swords", setOf(Strength, Speed)) {
    override fun description() = "Fighting with swords."
    override fun verb() = "sword fighting"
}
