package world.gen

import actors.NPC
import actors.Ox
import world.gen.biomes.Biome
import world.gen.biomes.Plain
import world.gen.biomes.Scrub
import world.gen.habitats.*

class AnimalSpawn(
    val spawn: ()->NPC,
    val biomes: Set<Biome>,
    val habitats: Set<Habitat>,
    val frequency: Float,
)

fun animalSpawns() = listOf<AnimalSpawn>(

    AnimalSpawn({ Ox() },
        setOf(Plain, Scrub),
        setOf(TemperateA, TemperateB, TropicalA, TropicalB, AlpineA, AlpineB),
        1f
    ),

)
