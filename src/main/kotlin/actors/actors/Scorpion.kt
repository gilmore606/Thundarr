package actors.actors

import actors.abilities.Leap
import actors.abilities.Sting
import actors.animations.Jump
import actors.bodyparts.*
import actors.states.IdleWander
import kotlinx.serialization.Serializable
import render.sparks.GooGore
import render.tilesets.Glyph
import things.BugCorpse
import things.Clothing
import things.Gear
import util.XY
import world.stains.Goo

@Serializable
class Scorpion : NPC() {
    override val tag = Tag.SCORPION
    override fun glyph() = Glyph.SCORPION
    override fun shadowWidth() = 1.7f
    override fun shadowXOffset() = 0.1f
    override fun stepAnimation(dir: XY) = Jump(dir)
    override fun name() = "scorpion"
    override fun description() = "A dog-sized purple scorpion."
    override fun makeBody() = setOf(Head(), Torso(), Stinger(), Legs())
    override fun makeAbilities() = setOf(Sting())
    override fun bloodstain() = Goo()
    override fun gore() = GooGore()
    override fun corpse() = BugCorpse()
    override fun xpValue() = 30
    override fun hpMax() = 6f
    override fun onSpawn() {
        initStats(9, 9, 4, 8, 6, 1, 0)
    }

    override fun unarmedWeapon() = claws
    override fun unarmedDamage() = 3f

    override fun visualRange() = 7f
    override fun idleState() = IdleWander(0.4f)
    override fun opinionOf(actor: Actor) = when {
        actor.isHuman() -> Opinion.HATE
        else -> super.opinionOf(actor)
    }
}
