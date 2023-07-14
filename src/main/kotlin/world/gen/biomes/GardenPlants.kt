package world.gen.biomes

import world.gen.spawnsets.PlantSet
import things.Thing.Tag.*

object GardenPlants {
    val set = PlantSet().apply {
        add(1f, BERRYBUSH)
        add(1f, WILDFLOWERS)
        add(1f, POPPIES)
        add(1f, BLUEBELLS)
        add(1f, DANDYLIONS)
        add(1f, SUNFLOWER)
        add(1f, LIGHTFLOWER)
        add(1f, HONEYPODBUSH)
        add(0.2f, DREAMFLOWER)
        add(0.1f, DEATHFLOWER)
        add(1f, APPLETREE)
        add(1f, PEARTREE)
    }
}
