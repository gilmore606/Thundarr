package actors.abilities

import actors.actors.Actor
import actors.stats.Brains
import actors.stats.Heart
import actors.statuses.Afraid
import kotlinx.serialization.Serializable
import ui.panels.Console
import util.Dice
import util.distanceBetween
import world.level.Level

@Serializable
class Scare(
    val scareCooldown: Double = 0.0,
    val scareChance: Float = 1f,
    val range: Float = 1f,
    val spec: Spec = Spec()
) : Ability(scareCooldown, scareChance) {

    @Serializable
    class Spec(
        val scareMsg: String = "%Dn terrifies %dd!",
        val scareFailMsg: String = "%Dn waves %p arms ineffectually at %dd."
    )

    override fun shouldQueue(actor: Actor, target: Actor) = (distanceBetween(actor.xy, target.xy) <= range)

    override fun execute(actor: Actor, level: Level, target: Actor) {
        val me = Heart.get(actor) + Brains.get(actor)
        val him = Heart.get(target) + Brains.get(target)
        val bonus = (me - him) * 0.5f
        val roll = Heart.resolve(actor, bonus)
        if (roll > 0) {
            Console.sayAct("", spec.scareMsg, actor, target)
            target.addStatus(Afraid(actor.id, actor.dname()))
        } else {
            Console.sayAct("", spec.scareFailMsg, actor, target)
        }
    }
}
