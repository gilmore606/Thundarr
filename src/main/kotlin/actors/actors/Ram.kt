package actors.actors

import actors.abilities.Rushdown
import actors.animations.Jump
import actors.bodyparts.Bodypart
import actors.states.IdleWander
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Clothing
import things.FurHide
import things.RawMeat
import util.XY

@Serializable
class Ram : NPC() {

    override val tag = Tag.RAM
    override fun glyph() = Glyph.GOAT
    override fun shadowWidth() = 1.7f
    override fun shadowXOffset() = 0.1f
    override fun stepAnimation(dir: XY) = Jump(dir)
    override fun name() = "ram"
    override fun description() = "A stout goaty creature with blue wool and curled horns."
    override fun makeBody() = Bodypart.quadruped()
    override fun makeAbilities() = setOf(Rushdown())
    override fun corpseMeats() = setOf(RawMeat(), FurHide())
    override fun xpValue() = 25
    override fun hpMax() = 8f
    override fun onSpawn() {
        initStats(11, 9, 4, 10, 6, 1, 0)
    }

    override fun unarmedWeapons() = setOf(hooves, horns)
    override fun unarmedDamage() = 3f
    override fun skinArmorMaterial() = Clothing.Material.HIDE
    override fun skinArmor() = 1f

    override fun visualRange() = 9f
    override fun idleState() = IdleWander(0.6f)
    override fun opinionOf(actor: Actor) = when {
        actor.isSentient() -> Opinion.HATE
        else -> super.opinionOf(actor)
    }
    override fun aggroRange() = 3f
}
