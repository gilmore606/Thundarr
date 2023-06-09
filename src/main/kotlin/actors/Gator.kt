package actors

import actors.animations.Slide
import actors.states.IdleWander
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.XY
import world.terrains.Swamp
import world.terrains.Terrain

@Serializable
class Gator : NPC() {
    companion object {
        val walkableTerrains = listOf(Swamp)
    }

    override fun name() = "gator"
    override fun glyph() = Glyph.GATOR
    override fun description() = "A long dark green lizard with a huge toothy snout."
    override fun stepAnimation(dir: XY) = Slide(dir)
    override fun onSpawn() {
        Strength.set(this, 14f)
        Speed.set(this, 11f)
        Brains.set(this, 4f)
    }
    override fun isHuman() = false
    override fun armorTotal() = 2.0f
    override fun idleState() = IdleWander(0.35f)
    override fun canWalkOn(terrain: Terrain) = walkableTerrains.contains(terrain)
    override fun canSwimShallow() = true
    override fun canSwimDeep() = true
}
