package actors.actors

import actors.abilities.Leap
import actors.abilities.Projectile
import actors.animations.Jump
import actors.states.IdleWander
import actors.stats.skills.Throw
import kotlinx.serialization.Serializable
import render.sparks.GooGore
import render.tilesets.Glyph
import things.Clothing
import things.Damage
import things.Log
import util.XY
import world.stains.Goo

@Serializable
class Cactoid : NPC() {
    companion object {
        val projectile = Projectile.Spec(
            Glyph.YELLOW_PROJECTILE,
            "%Dn fires its spines and hits %dd's %part!",
            "%Dn fires its spines at %dd, but misses.",
            "%Dn fires its spines, which bounce harmlessly off %dd's %part.",
            Damage.PIERCE,
            3f,
        )
    }

    override val tag = Tag.CACTOID
    override fun glyph() = Glyph.CACTOID
    override fun shadowWidth() = 1.2f
    override fun name() = "cactoid"
    override fun description() = "A humanoid-shaped cactus creature with a single blood-red eye."
    override fun makeAbilities() = setOf(Projectile(2.0, 1f, 4f, projectile))
    override fun bloodstain() = Goo()
    override fun gore() = GooGore()
    override fun corpse() = null
    override fun corpseMeats() = setOf(Log())
    override fun xpValue() = 20
    override fun hpMax() = 6f
    override fun onSpawn() {
        initStats(9, 9, 4, 8, 6, 1, 0)
        Throw.set(this, 1)
    }

    override fun unarmedWeapon() = fist
    override fun unarmedDamage() = 3f
    override fun skinArmorMaterial() = Clothing.Material.HIDE
    override fun skinArmor() = 1f

    override fun visualRange() = 10f
    override fun aggroRange() = 7f
    override fun idleState() = IdleWander(0.4f)
    override fun opinionOf(actor: Actor) = when {
        actor.isHuman() -> Opinion.HATE
        else -> super.opinionOf(actor)
    }
}
