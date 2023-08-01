package actors.abilities

import actors.actors.Actor
import kotlinx.serialization.Serializable
import ui.panels.Console
import util.Dice
import world.level.Level

@Serializable
class Regenerate(
    val regenerateCooldown: Double = 0.0,
    val regenerateChance: Float = 1f,
    val atHealthiness: Float = 0.5f,
    val spec: Spec,
) : Ability(regenerateCooldown, regenerateChance) {
    @Serializable
    class Spec(
        val healFraction: Float = 0.5f,
        val healMsg: String = "%Dn looks healthier!",
    )

    override fun shouldQueue(actor: Actor, target: Actor) = actor.healthiness() <= atHealthiness

    override fun execute(actor: Actor, level: Level, target: Actor) {
        val max = (actor.hpMax() * spec.healFraction)
        val amount = Dice.float(max * 0.5f, max)
        Console.sayAct("", spec.healMsg, actor, target)
        actor.healDamage(amount)
    }
}
