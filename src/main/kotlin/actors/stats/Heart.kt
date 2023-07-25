package actors.stats

import actors.actors.Actor
import actors.actors.Player


object Heart : Stat(Tag.HRT, "heart") {
    override fun description() = "Great-hearted beings endure and withstand more, and gain more power over magic."
    override fun verb() = "the heart"
    override fun examineSpecialStat() = "Health max"
    override fun examineSpecialStatValue(actor: Actor) = getHpMax(actor).toString()

    fun getHpMax(actor: Actor, total: Float = get(actor)) = (total * 2f)

    override fun onUpdate(actor: Actor, newTotal: Float) {
        if (actor is Player) {
            actor.currentHpMax = getHpMax(actor, newTotal)
        }
    }
}
