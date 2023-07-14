package world.gen.spawnsets

import things.Thing
import world.gen.habitats.Habitat

class PlantSet : SpawnSet<Thing.Tag, Thing, Habitat>() {

    fun add(freq: Float, item: Thing.Tag, fertMin: Float = 0f, fertMax: Float = 1f,
            only: Habitat? = null, include: Set<Habitat>? = null, exclude: Set<Habitat>? = null) {
        addEntry(freq, item, limit = Pair((fertMin*100f).toInt(), (fertMax*100f).toInt()),
            only, include, exclude)
    }

    fun getPlant(fertility: Float, habitat: Habitat) = roll((fertility * 100f).toInt(), habitat)

}
