package actors.stats.skills

import actors.stats.Speed
import actors.stats.Strength

object Throw : Skill(Tag.THROW, "throw", setOf(Strength, Speed)) {
    override fun description() = "Hitting targets from afar with thrown objects."
}
