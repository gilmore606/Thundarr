package world.gen.spawnsets

import actors.actors.NPC
import world.gen.habitats.Habitat

class AnimalSet : SpawnSet<NPC.Tag, NPC, Habitat>() {

    fun add(freq: Float, animal: NPC.Tag, levelMin: Int = 0, levelMax: Int = 100,
        only: Habitat? = null, include: Set<Habitat>? = null, exclude: Set<Habitat>? = null) {
        addEntry(freq, animal, limit = Pair(levelMin, levelMax), only = only, include = include, exclude = exclude)
    }

    fun add(freq: Float, subset: AnimalSet, levelMin: Int = 0, levelMax: Int = 100) {
        set.add(SubsetEntry(freq, subset, limit = Pair(levelMin, levelMax)))
    }

    fun getAnimal(level: Int, habitat: Habitat) = roll(level, habitat)

}
