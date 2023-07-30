package actors.actors

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
class Killdaisy : NPC() {
    override val tag = Tag.KILLDAISY
    override fun glyph() = Glyph.KILLDAISY
    override fun name() = "killdaisy"
    override fun description() = "A large floppy white flower blossom, brought to hideous life by unknown sorcery."
    override fun corpse() = null
    override fun canSwimShallow() = true
    override fun bloodstain() = null
    override fun gore() = GooGore()
    override fun stepAnimation(dir: XY) = Jump(dir)
    override fun hpMax() = 6f
    override fun onSpawn() {
        initStats(6, 12, 6, 13, 10, 1, 0)
    }
    override fun unarmedWeapon() = branches
    override fun unarmedDamage() = 3f

    override fun visualRange() = 8f
    override fun idleState() = IdleWander(0.4f)
    override fun opinionOf(actor: Actor) = when {
        actor.isHuman() -> NPC.Opinion.HATE
        else -> super.opinionOf(actor)
    }
}
