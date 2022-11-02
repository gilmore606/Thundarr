package actors.stats

object Speed : Stat(Tag.SPD, "speed") {
    override fun description() = "Quickness and agility.  Speedy beings move fast and are good with their mandibles."
    override fun verb() = "moving fast"
}
