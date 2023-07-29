package actors.actors

import actors.states.IdleWander
import kotlinx.serialization.Serializable
import render.tilesets.Glyph

@Serializable
class WoodSpider : NPC() {
    override val tag = Tag.WOOD_SPIDER
    override fun glyph() = Glyph.BROWN_SPIDER
    override fun shadowWidth() = 1.5f
    override fun shadowXOffset() = 0.2f
    override fun name() = "wood spider"
    override fun description() = "A huge brown bristle-bodied spider."
    override fun hpMax() = 8f
    override fun onSpawn() {
        initStats(9, 9, 3, 8, 6, 1, 0)
    }

    override fun unarmedWeapon() = mandibles
    override fun unarmedDamage() = 4f

    override fun visualRange() = 8f
    override fun idleState() = IdleWander(0.4f)
}
