package actors.stats

import actors.actors.Actor


object Strength : Stat(Tag.STR, "strength") {
    override fun description() = "Strong beings carry a lot and hit hard."
    override fun verb() = "smashing"
    override fun examineSpecialStat() = "Carrying capacity"
    override fun examineSpecialStatValue(actor: Actor) = String.format("%.1f", actor.carryingCapacity()) + "lb"
}
