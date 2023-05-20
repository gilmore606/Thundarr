package actors

import actors.states.IdleHerd
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import kotlinx.serialization.Serializable
import render.tilesets.Glyph

@Serializable
sealed class GenericPidgey : NPC() {
    override fun shadowWidth() = 1.3f
    override fun isHuman() = false
    override fun armorTotal() = 0.0f
    override fun idleState() = IdleHerd(
        0.6f, 12, false,
        22.0f,
        6.0f
    )
}

@Serializable
class Pidgey : GenericPidgey() {
    override fun name() = "pidgey"
    override fun glyph() = Glyph.PIDGEY
    override fun description() = "A birdlike human figure, covered in feathers, with a bulbous head and beady black eyes."
    override fun onSpawn() {
        Strength.set(this, 6f)
        Speed.set(this, 13f)
        Brains.set(this, 6f)
    }
}

@Serializable
class PidgeyBrute : GenericPidgey() {
    override fun name() = "pidgey brute"
    override fun glyph() = Glyph.PIDGEY_BRUTE
    override fun description() = "A burly birdlike human figure, covered in feathers, with a bulbous head and menacing black eyes."
    override fun onSpawn() {
        Strength.set(this, 11f)
        Speed.set(this, 12f)
        Brains.set(this, 5f)
    }
}
