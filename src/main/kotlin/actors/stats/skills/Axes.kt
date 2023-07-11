package actors.stats.skills

import actors.stats.Strength

object Axes : Skill(Tag.AXES, "axes", setOf(Strength)) {
    override fun description() = "Fighting with axes."
    override fun verb() = "axe fighting"
}
