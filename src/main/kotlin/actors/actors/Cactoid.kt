package actors.actors

import actors.abilities.Leap
import actors.animations.Jump
import actors.states.IdleWander
import kotlinx.serialization.Serializable
import render.sparks.GooGore
import render.tilesets.Glyph
import things.Clothing
import things.Log
import util.XY
import world.stains.Goo

@Serializable
class Cactoid : NPC() {
    override val tag = Tag.CACTOID
    override fun glyph() = Glyph.CACTOID
    override fun shadowWidth() = 1.2f
    override fun name() = "cactoid"
    override fun description() = "A humanoid-shaped cactus creature with a single blood-red eye."
    override fun bloodstain() = Goo()
    override fun gore() = GooGore()
    override fun corpse() = null
    override fun corpseMeats() = setOf(Log())
    override fun hpMax() = 6f
    override fun onSpawn() {
        initStats(9, 9, 4, 8, 6, 1, 0)
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
