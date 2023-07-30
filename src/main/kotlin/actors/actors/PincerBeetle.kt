package actors.actors

import actors.animations.Slide
import actors.bodyparts.Bodypart
import actors.states.IdleWander
import kotlinx.serialization.Serializable
import render.sparks.GooGore
import render.tilesets.Glyph
import things.BugCorpse
import things.Clothing
import util.XY
import world.stains.Goo

@Serializable
class PincerBeetle : NPC() {

    override val tag = Tag.PINCER_BEETLE
    override fun glyph() = Glyph.PINCER_BEETLE
    override fun shadowWidth() = 1.5f
    override fun shadowXOffset() = 0.2f
    override fun stepAnimation(dir: XY) = Slide(dir)
    override fun name() = "pincer beetle"
    override fun description() = "A huge shiny purple beetle, with large pinching jaws."
    override fun makeBody() = Bodypart.quadruped()
    override fun bloodstain() = Goo()
    override fun gore() = GooGore()
    override fun corpse() = BugCorpse()
    override fun xpValue() = 30
    override fun hpMax() = 6f
    override fun onSpawn() {
        initStats(9, 9, 3, 8, 6, 1, 0)
    }

    override fun unarmedWeapon() = mandibles
    override fun unarmedDamage() = 4f
    override fun skinArmorMaterial() = Clothing.Material.SHELL
    override fun skinArmor() = 1f

    override fun visualRange() = 8f
    override fun idleState() = IdleWander(0.4f)
    override fun opinionOf(actor: Actor) = when {
        actor.isSentient() -> Opinion.HATE
        else -> super.opinionOf(actor)
    }
}
