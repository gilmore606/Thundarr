package actors.actors

import actors.states.IdleWander
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Clothing
import things.ScalyHide

@Serializable
class Geckoid : NPC() {
    override val tag = Tag.GECKOID
    override fun glyph() = Glyph.GECKOID
    override fun name() = "geckoid"
    override fun description() = "A scaly green humanoid with beady red eyes, clad in raw hides."
    override fun isSentient() = true
    override fun corpseMeats() = setOf(ScalyHide())
    override fun xpValue() = 25
    override fun hpMax() = 9f
    override fun onSpawn() {
        initStats(9, 9, 4, 9, 6, 2, 0)
    }

    override fun unarmedWeapon() = spear
    override fun unarmedDamage() = 3f
    override fun skinArmorMaterial() = Clothing.Material.SCALES
    override fun skinArmor() = 1f

    override fun visualRange() = 10f
    override fun aggroRange() = 7f
    override fun idleState() = IdleWander(0.4f)
    override fun opinionOf(actor: Actor) = when {
        actor.isHuman() -> Opinion.HATE
        else -> super.opinionOf(actor)
    }
}
