package actors.stats

object Senses : Stat(Tag.SEN, "senses") {
    override fun description() = "High senses provides greater awareness."
    override fun verb() = "noticing"
}
