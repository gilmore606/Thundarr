package actors.stats.skills

import actors.stats.Brains

object Survive : Skill(Tag.SURVIVE, "survive", setOf(Brains)) {
    override fun description() = "Making fires, scavenging, finding shelter, and other outdoorsy crafts."
    override fun verb() = "survival"
}
