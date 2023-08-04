package world.gen

import actors.actors.NPC
import util.XY
import world.Chunk

interface AnimalSpawnSource {
    fun animalSpawnPoint(chunk: Chunk, animal: NPC, near: XY? = null, within: Float? = null): XY?
}
