package actors.stats.skills

import actors.stats.Brains
import actors.stats.Strength

object Build : Skill(Tag.BUILD, "build", setOf(Strength, Brains)) {
    override fun description() = "Building walls and other large structures."
}