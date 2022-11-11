package actors.stats

object Heart : Stat(Tag.HRT, "heart") {
    override fun description() = "Great-hearted beings endure and withstand more, and gain more power over magic."
    override fun verb() = "the heart"
}
