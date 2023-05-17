package world.gen

import actors.*
import util.Dice
import world.gen.biomes.Biome
import world.gen.biomes.Plain
import world.gen.biomes.Scrub
import world.gen.habitats.*

class AnimalSpawn(
    val tag: ()->NPC.Tag,
    val biomes: Set<Biome>,
    val habitats: Set<Habitat>,
    val min: Int,
    val max: Int,
    val frequency: Float,
)

fun animalSpawns() = listOf<AnimalSpawn>(

    AnimalSpawn(
        { NPC.Tag.NPC_AUROX },
        setOf(Plain, Scrub),
        setOf(TemperateA, TemperateB, TropicalA, TropicalB, AlpineA, AlpineB),
        2, 4, 0.3f
    ),
    AnimalSpawn(
        { if (Dice.chance(0.5f)) NPC.Tag.NPC_TUSKER else NPC.Tag.NPC_TUSKLET },
        setOf(Plain, Scrub),
        setOf(TemperateA, TemperateB, TropicalA, TropicalB, AlpineA, AlpineB),
        2, 6, 0.4f
    ),
    AnimalSpawn(
        { NPC.Tag.NPC_CYCLOX },
        setOf(Scrub),
        setOf(TemperateA, TemperateB, TropicalA, TropicalB, AlpineA, AlpineB),
        1, 2, 0.1f
    ),

)
