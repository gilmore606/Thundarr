package actors.actors

import actors.states.IdleHerd
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Clothing

@Serializable
sealed class GenericTortle : NPC() {
    override fun glyph() = Glyph.TORTLE
    override fun shadowWidth() = 1.4f
    override fun skinArmorMaterial() = Clothing.Material.SHELL
    override fun unarmedWeapon() = hooves
    override fun idleState() = IdleHerd(
        0.3f, 8, true,
        19.0f,
        6.0f
    )
}

@Serializable
class YoungTortle : GenericTortle() {
    override fun hue() = 0.9f
    override fun name() = "young tortle"
    override fun description() = "A large tortoise with a large raised head and sharp horns."
    override fun onSpawn() {
        xpLevel = 1
        hpMax = 9f
        initStats(12, 6, 8, 9, 9, 1, 0)
    }
    override fun unarmedDamage() = 3f
    override fun skinArmor() = 2.5f
}

@Serializable
class Tortle : GenericTortle() {
    override fun name() = "tortle"
    override fun description() = "A large tortoise with a large raised head and sharp horns."
    override fun onSpawn() {
        xpLevel = 2
        hpMax = 12f
        initStats(13, 6, 8, 9, 9, 2, 0)
    }
    override fun unarmedDamage() = 4f
    override fun skinArmor() = 2.5f
}

@Serializable
class BullTortle : GenericTortle() {
    override fun hue() = -0.8f
    override fun name() = "bull tortle"
    override fun description() = "A huge tortoise with a large raised head, sharp horns, and a gray crest."
    override fun onSpawn() {
        xpLevel = 3
        hpMax = 18f
        initStats(15, 8, 8, 9, 9, 3, 0)
    }
    override fun unarmedDamage() = 5f
    override fun skinArmor() = 2.5f
}
