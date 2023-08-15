package actors.actors

import actors.states.IdleDen
import actors.states.IdleWander
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import actors.stats.skills.Fight
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Clothing
import things.FurHide
import world.Entity

@Serializable
class Wolfman : NPC() {

    override val tag = Tag.WOLFMAN
    override fun glyph() = Glyph.WOLFMAN
    override fun name() = "wolfman"
    override fun gender() = Entity.Gender.MALE
    override fun description() = "A snarling wolf, on two legs...no, a man with...Demon dogs!!"
    override fun xpValue() = 100
    override fun hpMax() = 30f
    override fun onSpawn() {
        initStats(12, 12, 6, 13, 10, 4, 2)
    }
    override fun corpseMeats() = setOf(FurHide())
    override fun unarmedWeapons() = setOf(claws, teeth)
    override fun unarmedDamage() = 5f
    override fun skinArmorMaterial() = Clothing.Material.FUR
    override fun skinArmor() = 2f

    override fun isSentient() = true
    override fun spawnsInDen() = true
    override fun idleState() = IdleDen(
        0.5f, 10, true, 10.0f, 17.0f
    )
    override fun aggroRange() = 8f
    override fun visualRange() = 10f
    override fun canSeeInDark() = true
    override fun opinionOf(actor: Actor) = when {
        actor.isHuman() -> Opinion.HATE
        else -> super.opinionOf(actor)
    }
}
