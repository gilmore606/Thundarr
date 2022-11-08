package actors.stats.skills

import actors.stats.Brains

object Medic : Skill(Tag.MEDIC, "medic", setOf(Brains)) {
    override fun description() = "Bandaging wounds and treating conditions."
    override fun verb() = "medical treatment"
}
