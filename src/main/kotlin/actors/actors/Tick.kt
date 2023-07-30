package actors.actors

import actors.animations.Jump
import actors.bodyparts.Bodypart
import actors.states.IdleWander
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Clothing
import util.XY

@Serializable
class Tick : NPC() {
    override val tag = Tag.TICK
    override fun glyph() = Glyph.TICK
    override fun shadowWidth() = 1.3f
    override fun stepAnimation(dir: XY) = Jump(dir)
    override fun name() = "tick"
    override fun description() = "A large bloated insect with tiny red eyes and many legs."
    override fun makeBody() = Bodypart.blob()
    override fun corpse() = null
    override fun xpValue() = 15
    override fun hpMax() = 5f
    override fun onSpawn() {
        initStats(9, 9, 4, 8, 6, 1, 0)
    }

    override fun unarmedWeapon() = teeth
    override fun unarmedDamage() = 3f
    override fun skinArmorMaterial() = Clothing.Material.SHELL
    override fun skinArmor() = 1f

    override fun visualRange() = 7f
    override fun aggroRange() = 5f
    override fun idleState() = IdleWander(0.4f)
    override fun opinionOf(actor: Actor) = when {
        actor.isHuman() -> Opinion.HATE
        else -> super.opinionOf(actor)
    }
}
