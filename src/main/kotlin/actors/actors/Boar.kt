package actors.actors

import actors.bodyparts.Bodypart
import actors.states.IdleWander
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.RawMeat

@Serializable
class Boar : NPC() {
    override val tag = Tag.BOAR
    override fun glyph() = Glyph.BOAR
    override fun shadowWidth() = 1.3f
    override fun name() = "boar"
    override fun description() = "A wild boar with large yellow tusks."
    override fun makeBody() = Bodypart.quadruped()
    override fun canSwimShallow() = true
    override fun xpValue() = 50
    override fun hpMax() = 8f
    override fun onSpawn() {
        initStats(12, 12, 6, 13, 10, 4, 2)
    }
    override fun corpseMeats() = setOf(RawMeat())
    override fun unarmedWeapon() = hooves
    override fun unarmedDamage() = 3f
    override fun skinArmor() = 1f

    override fun visualRange() = 8f
    override fun canSeeInDark() = true
    override fun idleState() = IdleWander(0.4f)
    override fun opinionOf(actor: Actor) = when {
        actor.isSentient() -> Opinion.HATE
        else -> super.opinionOf(actor)
    }
    override fun aggroRange() = 3f
}
