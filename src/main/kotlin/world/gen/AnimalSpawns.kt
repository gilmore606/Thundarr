package world.gen

import actors.actors.NPC
import util.Dice
import world.gen.biomes.*
import world.gen.habitats.*

class AnimalSpawn(
    val tag: ()-> NPC.Tag,
    val biomes: Set<Biome>,
    val habitats: Set<Habitat>,
    val minThreat: Float,
    val maxThreat: Float,
    val min: Int,
    val max: Int,
    val frequency: Float,
)

fun animalSpawns() = listOf<AnimalSpawn>(

    AnimalSpawn(
        { NPC.Tag.AUROX },
        setOf(Plain, Scrub),
        setOf(TemperateA, TemperateB, TropicalA, TropicalB, AlpineA, AlpineB),
        0f, 1000f, 2, 4, 0.3f
    ),
    AnimalSpawn(
        { if (Dice.chance(0.5f)) NPC.Tag.TUSKER else NPC.Tag.TUSKLET },
        setOf(Scrub, Hill, ForestHill),
        setOf(TemperateA, TemperateB, TropicalA, TropicalB, AlpineA, AlpineB),
        0f, 1000f, 2, 6, 0.4f
    ),
    AnimalSpawn(
        { NPC.Tag.CYCLOX },
        setOf(Scrub, Desert),
        setOf(TemperateA, TemperateB, TropicalA, TropicalB, AlpineA, AlpineB),
        0f, 1000f, 1, 2, 0.1f
    ),
    AnimalSpawn(
        { if (Dice.chance(0.5f)) NPC.Tag.VOLTELOPE else NPC.Tag.VOLTELOPE_FAWN },
        setOf(Plain, Scrub),
        setOf(TemperateA, TemperateB, TropicalA, TropicalB, AlpineA, AlpineB),
        0f, 1000f, 2, 5, 0.3f
    ),

    AnimalSpawn(
        { NPC.Tag.SALAMAN },
        setOf(Swamp),
        setOf(TemperateA, TemperateB, TropicalA, TropicalB, AlpineA, AlpineB),
        0f, 1000f, 1, 2, 0.4f
    ),
    AnimalSpawn(
        { NPC.Tag.TORTLE },
        setOf(Swamp, Hill),
        setOf(TemperateA, TemperateB, TropicalA, TropicalB, AlpineA, AlpineB),
        0f, 1000f, 1, 2, 0.2f
    ),

    AnimalSpawn(
        { if (Dice.chance(0.7f)) NPC.Tag.PIDGEY else NPC.Tag.PIDGEY_BRUTE },
        setOf(Forest, ForestHill),
        setOf(TemperateA, TemperateB, TropicalA, TropicalB, AlpineA, AlpineB),
        0f, 1000f, 2, 3, 0.3f
    )

)
