package actors.stats.skills

import actors.stats.Senses
import actors.stats.Speed

object Guns : Skill(Tag.GUNS, "guns", setOf(Senses)) {
    override fun description() = "Accurately shooting guns and other aimed weapons."
    override fun verb() = "gun fighting"
}
