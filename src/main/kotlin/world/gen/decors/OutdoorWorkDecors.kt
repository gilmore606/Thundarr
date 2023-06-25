package world.gen.decors

import kotlinx.serialization.Serializable
import things.Gravestone
import things.Shrine
import util.Dice
import world.terrains.Terrain

@Serializable
class HuntingGround : Decor() {
    override fun description() = "The ground here is well-trodden."
    override fun announceJobMsg() = listOf(
        "Time to hunt.",
        "Let's see what the Lords of Light provide us today.",
        "Hunting time."
    ).random()

    override fun workAreaComments() = mutableSetOf(
        "Be very quiet, you'll scare the game.",
        "The land will provide what I need.",
        "It's hard living out here, but it's free."
    )

    override fun doFurnish() {

    }
}

@Serializable
class MeditationSpot: Decor() {
    override fun description() = "The air here is still, and you feel strangely at peace."
    override fun announceJobMsg() = listOf(
        "Time for meditation.",
        "I must go and pray.",
        "The Lords of Light call to me.",
    ).random()
    override fun workAreaComments() = mutableSetOf(
        "Hear me, Lords of Light!",
        "Speak to me, Lords of Light!",
        "Bless me Lords of Light, your humble servant.",
        "O Lords, cleanse this body of sin!"
    )
    override fun doFurnish() {
        val groundTerrain = if (Dice.flip()) Terrain.Type.TERRAIN_STONEFLOOR else Terrain.Type.TERRAIN_SAND
        if (Dice.flip()) {
            atCenter {
                spawn(Shrine())
                terrainAround(groundTerrain)
            }
        } else {
            awayFromWall {
                spawn(if (Dice.flip()) Shrine() else Gravestone())
                terrainAround(groundTerrain)
            }
        }
    }
}
