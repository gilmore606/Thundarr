package actors.actors

import actors.animations.Jump
import actors.bodyparts.Bodypart
import actors.states.IdleWander
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.XY

@Serializable
class Pengo : NPC() {
    override val tag = Tag.PENGO
    override fun glyph() = Glyph.PENGUIN
    override fun name() = "pengo"
    override fun description() = "A squat, fat flightless bird.  It's very well dressed."
    override fun makeBody() = Bodypart.bird()
    override fun hpMax() = 8f
    override fun onSpawn() {
        initStats(11, 9, 4, 10, 6, 1, 0)
    }

    override fun unarmedWeapon() = beak
    override fun unarmedDamage() = 3f

    override fun visualRange() = 9f
    override fun idleState() = IdleWander(0.6f)
}
