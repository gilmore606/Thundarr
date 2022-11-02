package actors.stats.skills

import actors.stats.Speed
import actors.stats.Strength

object Throw : Skill(Tag.THROW, "throw", setOf(Strength, Speed))
