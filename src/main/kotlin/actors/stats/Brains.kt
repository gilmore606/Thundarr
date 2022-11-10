package actors.stats

object Brains : Stat(Tag.BRN, "brains") {
    override fun description() = "Brainy beings can notice and understand things, and use tech and magic."
    override fun verb() = "thinking"
}
