package world.gen

import actors.NPC
import util.XY
import world.Chunk

interface AnimalSpawnSource {
    fun animalSpawnPoint(chunk: Chunk, animalType: NPC.Tag): XY?
}
