package actors.stats

object Speed : Stat(Tag.SPD, "speed") {
    override fun description() = "Speedy beings move quick and are good with their mandibles."
    override fun verb() = "moving fast"
}
