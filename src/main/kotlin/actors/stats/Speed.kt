package actors.stats

import actors.Actor

object Speed : Stat(Tag.SPD, "speed") {
    override fun description() = "Speedy beings move quick and are good with their mandibles."
    override fun verb() = "moving fast"
    override fun examineSpecialStat() = "Action speed"
    override fun examineSpecialStatValue(actor: Actor) = String.format("%.1f", actor.actionSpeed())
}
