package actors.abilities

import actors.actors.Actor
import kotlinx.serialization.Serializable
import world.level.Level

@Serializable
class Bloodsuck(
    val bloodsuckCooldown: Double = 0.0,
    val bloodsuckChance: Float = 1f,
    val atHealthiness: Float = 0.95f,

) : Ability(bloodsuckCooldown, bloodsuckChance) {
    @Serializable
    class Spec(
        val suckFraction: Float = 0.25f,
        val suckEfficiency: Float = 1f,
        val suckMsg: String = "%Dn "
    )
    override fun shouldQueue(actor: Actor, target: Actor): Boolean {
        return false
    }

    override fun execute(actor: Actor, level: Level, target: Actor) {

    }
}
