package actors.stats

object Brains : Stat(Tag.BRN, "brains") {
    override fun description() = "Brainpower.  Brainy beings can use tech and magic and notice things."
    override fun verb() = "thinking"
}
