package actors.stats.skills

import actors.Actor
import actors.stats.Brains

object Science : Skill(Tag.SCIENCE, "science", setOf(Brains)) {
    override fun description() = "Making use of ancient tech."
    override fun verb() = "science devices"
    override fun getDefaultBase(actor: Actor) = -4f
}
