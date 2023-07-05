package world.gen.decors

import actors.jobs.HuntingJob
import actors.jobs.MeditationJob
import kotlinx.serialization.Serializable
import things.Gravestone
import things.Shrine
import util.Dice
import world.terrains.Terrain

@Serializable
class HuntingGround : Decor() {
    override fun description() = "The ground here is well-trodden."
    override fun job() = HuntingJob(room.rect)

    override fun doFurnish() {

    }
}

@Serializable
class MeditationSpot: Decor() {
    override fun description() = "The air here is still, and you feel strangely at peace."
    override fun job() = MeditationJob(room.rect)

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
