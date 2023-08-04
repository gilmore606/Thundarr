package world.gen.spawnsets

import actors.actors.NPC
import world.gen.habitats.Habitat

class AnimalSet : SpawnSet<AnimalSet.Group, NPC, Habitat>() {

    class Group(
        val tags: List<NPC.Tag> = listOf(),
        val radius: Float? = null,
        val countsAsOne: Boolean = false,
    )

    fun addGroup(freq: Float, animal: NPC.Tag, count: ()->Int, radius: Float = 20f, countsAsOne: Boolean = false,
                 levelMin: Int = 0, levelMax: Int = 100,
                 only: Habitat? = null, include: Set<Habitat>? = null, exclude: Set<Habitat>? = null) {
        val tags = mutableListOf<NPC.Tag>().apply  {
            repeat(count.invoke()) { add(animal) }
        }
        addEntry(freq, Group(tags, radius, countsAsOne), limit = Pair(levelMin, levelMax), only = only, include = include, exclude = exclude)
    }

    fun add(freq: Float, animal: NPC.Tag, levelMin: Int = 0, levelMax: Int = 100,
            only: Habitat? = null, include: Set<Habitat>? = null, exclude: Set<Habitat>? = null) {
        add(freq, listOf(animal), levelMin, levelMax, only, include, exclude)
    }
    fun add(freq: Float, animals: List<NPC.Tag>, levelMin: Int = 0, levelMax: Int = 100,
        only: Habitat? = null, include: Set<Habitat>? = null, exclude: Set<Habitat>? = null) {
        addEntry(freq, Group(animals), limit = Pair(levelMin, levelMax), only = only, include = include, exclude = exclude)
    }


    fun add(freq: Float, subset: AnimalSet, levelMin: Int = 0, levelMax: Int = 100) {
        set.add(SubsetEntry(freq, subset, limit = Pair(levelMin, levelMax)))
    }

    fun getAnimals(level: Int, habitat: Habitat) = roll(level, habitat)?.item ?: Group()

}
