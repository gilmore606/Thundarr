package actors.actors

import actors.animations.Slide
import actors.bodyparts.Bodypart
import actors.states.IdleWander
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Clothing
import things.RawMeat
import things.ScalyHide
import things.Thing
import util.XY
import world.terrains.DeepWater
import world.terrains.ShallowWater
import world.terrains.Swamp
import world.terrains.Terrain

@Serializable
class Gator : NPC() {
    companion object {
        val walkableTerrains = listOf(Swamp, ShallowWater, DeepWater)
    }

    override val tag = Tag.GATOR
    override fun name() = "gator"
    override fun glyph() = Glyph.GATOR
    override fun description() = "A long dark green lizard with a huge toothy snout."
    override fun makeBody() = Bodypart.quadruped()
    override fun stepAnimation(dir: XY) = Slide(dir)
    override fun xpValue() = 80
    override fun hpMax() = 28f
    override fun onSpawn() {
        initStats(14, 11, 4, 9, 10, 2, 1)
    }
    override fun unarmedWeapon() = teeth
    override fun unarmedDamage() = 6f
    override fun skinArmorMaterial() = Clothing.Material.SCALES
    override fun skinArmor() = 2.5f
    override fun idleState() = IdleWander(0.35f)
    override fun opinionOf(actor: Actor) = when {
        actor.isSentient() -> Opinion.HATE
        else -> super.opinionOf(actor)
    }
    override fun canWalkOn(terrain: Terrain) = walkableTerrains.contains(terrain)
    override fun canSwimShallow() = true
    override fun canSwimDeep() = true
    override fun corpseMeats() = mutableSetOf<Thing>().apply {
        add(RawMeat())
        add(ScalyHide())
    }
}
