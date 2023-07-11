package actors.stats.skills

import actors.stats.Senses
import actors.stats.Speed

object Clubs : Skill(Tag.CLUBS, "clubs", setOf(Speed, Senses)) {
    override fun description() = "Fighting with bludgeoning weapons."
    override fun verb() = "club fighting"
}
