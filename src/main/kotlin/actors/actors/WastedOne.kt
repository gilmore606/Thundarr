package actors.actors

import actors.states.IdleDen
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import world.Entity

@Serializable
class WastedOne : NPC() {

    override val tag = Tag.WASTED_ONE
    override fun glyph() = Glyph.WASTED_ONE
    override fun name() = "wasted one"
    override fun gender() = Entity.Gender.NEUTER
    override fun description() = "A tall dessicated humanoid with sparkling eyes."
    override fun isSentient() = true
    override fun xpValue() = 120
    override fun hpMax() = 50f
    override fun onSpawn() {
        initStats(15, 8, 4, 12, 12, 3, 0)
    }
    override fun unarmedWeapons() = setOf(fist, teeth)
    override fun unarmedDamage() = 3.5f
    override fun skinArmor() = 2f

    override fun spawnsInDen() = true
    override fun idleState() = IdleDen(
        0.25f, 8, true
    )
    override fun aggroRange() = 4f
    override fun visualRange() = 7f
    override fun canSeeInDark() = true
    override fun opinionOf(actor: Actor) = when {
        actor.isSentient() -> Opinion.HATE
        else -> super.opinionOf(actor)
    }
}
