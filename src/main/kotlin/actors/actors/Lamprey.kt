package actors.actors

import actors.abilities.Bloodsuck
import actors.animations.Slide
import actors.bodyparts.Bodypart
import actors.states.IdleWander
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Clothing
import util.XY

@Serializable
class Lamprey : NPC() {
    override val tag = Tag.LAMPREY
    override fun glyph() = Glyph.LAMPREY
    override fun shadowWidth() = 1.5f
    override fun shadowXOffset() = 0.1f
    override fun stepAnimation(dir: XY) = Slide(dir)
    override fun name() = "lamprey"
    override fun description() = "A writhing flesh tube with a gaping toothy mouth hole.  Ugh."
    override fun makeBody() = Bodypart.serpent()
    override fun makeAbilities() = setOf(Bloodsuck())
    override fun xpValue() = 20
    override fun hpMax() = 6f
    override fun onSpawn() {
        initStats(9, 9, 4, 8, 6, 1, 0)
    }
    override fun canSwimShallow() = true

    override fun unarmedWeapon() = teeth
    override fun unarmedDamage() = 3f
    override fun skinArmorMaterial() = Clothing.Material.HIDE
    override fun skinArmor() = 1f

    override fun visualRange() = 6f
    override fun idleState() = IdleWander(0.4f)
    override fun opinionOf(actor: Actor) = when {
        actor.isHuman() -> Opinion.HATE
        else -> super.opinionOf(actor)
    }
}
