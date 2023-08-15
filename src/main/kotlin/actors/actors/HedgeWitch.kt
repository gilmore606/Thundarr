package actors.actors

import actors.states.IdleDen
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import world.Entity

@Serializable
class HedgeWitch : NPC() {

    override val tag = Tag.HEDGE_WITCH
    override fun glyph() = Glyph.WITCH
    override fun name() = "hedge witch"
    override fun gender() = Entity.Gender.FEMALE
    override fun description() = "A wizened old crone with tangled hair and an evil look."
    override fun isSentient() = true
    override fun isHuman() = true
    override fun xpValue() = 100
    override fun hpMax() = 15f
    override fun onSpawn() {
        initStats(8, 12, 13, 14, 12, 3, 3)
    }
    override fun unarmedWeapons() = setOf(claws, teeth)
    override fun unarmedDamage() = 4f

    override fun spawnsInDen() = true
    override fun idleState() = IdleDen(
        0.6f, 12, true, 14f, 19f
    )
    override fun aggroRange() = 10f
    override fun visualRange() = 10f
    override fun canSeeInDark() = true
    override fun opinionOf(actor: Actor) = when {
        actor.isHuman() -> Opinion.HATE
        else -> super.opinionOf(actor)
    }
}
