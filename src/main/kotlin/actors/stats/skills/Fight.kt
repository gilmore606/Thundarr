package actors.stats.skills

import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength

object Fight : Skill(Tag.FIGHT, "fight", setOf(Strength, Speed, Brains)) {
    override fun description() = "Hitting other beings with fists, claws or objects."
    override fun verb() = "fighting"
}

