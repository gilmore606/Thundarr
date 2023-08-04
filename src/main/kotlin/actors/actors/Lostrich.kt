package actors.actors

import actors.animations.Jump
import actors.bodyparts.*
import actors.states.IdleWander
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Clothing
import things.RawMeat
import util.XY

@Serializable
class Lostrich : NPC() {
    override val tag = Tag.LOSTRICH
    override fun glyph() = Glyph.LOSTRICH
    override fun shadowWidth() = 1.7f
    override fun shadowXOffset() = 0.1f
    override fun stepAnimation(dir: XY) = Jump(dir)
    override fun name() = "lostrich"
    override fun description() = "A tall flightless bird.  Both its heads stare at you from serpentine necks."
    override fun makeBody() = setOf(Head(2), Body(), Legs())
    override fun corpseMeats() = setOf(RawMeat())
    override fun xpValue() = 25
    override fun hpMax() = 8f
    override fun onSpawn() {
        initStats(11, 9, 4, 10, 6, 1, 0)
    }

    override fun unarmedWeapon() = beak
    override fun unarmedDamage() = 3f

    override fun visualRange() = 9f
    override fun defendSpecies() = true
    override fun idleState() = IdleWander(0.6f)
}
