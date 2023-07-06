package world.gen.decors

import actors.jobs.HuntingJob
import actors.jobs.MeditationJob
import kotlinx.serialization.Serializable
import things.Bonepile
import things.Campfire
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

@Serializable
class MountainPeak : Decor() {
    override fun description() = "You take in the view from the mountain peak."
    override fun doFurnish() {
        if (Dice.chance(0.2f)) {
            atCenter {
                spawn(Shrine())
                terrainAround(if (Dice.flip()) Terrain.Type.TERRAIN_STONEFLOOR else Terrain.Type.TERRAIN_ROCKS)
            }
        }
        if (Dice.chance(0.7f)) {
            repeat (Dice.oneTo(5)) {
                awayFromWall {
                    spawn(if (Dice.chance(0.8f)) Bonepile() else Gravestone())
                }
            }
        }
        if (Dice.chance(0.5f)) {
            awayFromWall {
                spawn(Campfire())
                if (Dice.chance(0.2f)) {
                    terrainAround(if (Dice.flip()) Terrain.Type.TERRAIN_SAND else Terrain.Type.TERRAIN_CAVEFLOOR)
                }
            }
        }
    }
}
