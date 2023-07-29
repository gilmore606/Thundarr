package actors.actors

import actors.abilities.Leap
import actors.animations.Jump
import actors.states.IdleWander
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Clothing
import util.LightColor
import util.XY

@Serializable
class Glocust : NPC() {
    override val tag = Tag.GLOCUST
    override fun glyph() = Glyph.LOCUST
    override fun shadowWidth() = 1.5f
    override fun shadowXOffset() = 0.2f
    override fun stepAnimation(dir: XY) = Jump(dir)
    override fun name() = "glocust"
    override fun description() = "A large leaping insect, which emits a phosphorescent glow."
    override fun hpMax() = 6f
    override fun onSpawn() {
        initStats(9, 9, 3, 8, 6, 1, 0)
    }
    override fun initialAbilities() = setOf(Leap(3))
    override fun unarmedWeapon() = mandibles
    override fun unarmedDamage() = 4f
    override fun skinArmorMaterial() = Clothing.Material.SHELL
    override fun skinArmor() = 1f

    override fun visualRange() = 8f
    override fun idleState() = IdleWander(0.4f)
    override fun opinionOf(actor: Actor) = when {
        actor.isHuman() -> Opinion.HATE
        else -> super.opinionOf(actor)
    }

    val glow = LightColor(0f, 0.4f, 0.2f)
    override fun light() = glow
}