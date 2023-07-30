package actors.actors

import actors.bodyparts.Bodypart
import actors.states.IdleWander
import kotlinx.serialization.Serializable
import render.sparks.GooGore
import render.tilesets.Glyph
import things.BugCorpse
import world.stains.Goo

@Serializable
class WoodSpider : NPC() {
    override val tag = Tag.WOOD_SPIDER
    override fun glyph() = Glyph.BROWN_SPIDER
    override fun shadowWidth() = 1.5f
    override fun shadowXOffset() = 0.2f
    override fun name() = "wood spider"
    override fun description() = "A huge brown bristle-bodied spider."
    override fun makeBody() = Bodypart.quadruped()
    override fun bloodstain() = Goo()
    override fun gore() = GooGore()
    override fun corpse() = BugCorpse()
    override fun xpValue() = 30
    override fun hpMax() = 8f
    override fun onSpawn() {
        initStats(9, 9, 3, 8, 6, 1, 0)
    }

    override fun unarmedWeapon() = mandibles
    override fun unarmedDamage() = 4f

    override fun visualRange() = 8f
    override fun idleState() = IdleWander(0.4f)
    override fun opinionOf(actor: Actor) = when {
        actor.isSentient() -> Opinion.HATE
        else -> super.opinionOf(actor)
    }
}
