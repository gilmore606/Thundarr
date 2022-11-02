package actors.stats.skills

import actors.stats.Strength


object Dig : Skill(Tag.DIG, "dig", setOf(Strength)) {
    override fun description() = "Digging efficiently with tools through dirt and stone."
}
