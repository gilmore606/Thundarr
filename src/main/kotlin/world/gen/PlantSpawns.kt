package world.gen

import things.*
import world.gen.biomes.*
import world.gen.habitats.*

class PlantSpawn(
    val spawn: ()->Thing,
    val biomes: Set<Biome>,
    val habitats: Set<Habitat>,
    val frequency: Float,
    val minFertility: Float,
    val maxFertility: Float
)

fun plantSpawns() = listOf<PlantSpawn>(

    // Trees
    PlantSpawn({ OakTree() },
        setOf(Forest, ForestHill),
        setOf(TemperateA, TemperateB),
        1f, 0.7f, 1f
    ),
    PlantSpawn({ TeakTree() },
        setOf(Forest, ForestHill),
        setOf(TropicalA, TropicalB),
        1f, 0.7f, 1f
    ),
    PlantSpawn({ MapleTree() },
        setOf(Forest, ForestHill, Swamp),
        setOf(TemperateA, TemperateB),
        1f, 0.7f, 1f
    ),
    PlantSpawn({ BirchTree() },
        setOf(Forest, Plain, Swamp),
        setOf(TemperateA, TemperateB, TropicalA, TropicalB),
        1f, 0.7f, 1f
    ),
    PlantSpawn({ AppleTree() },
        setOf(Forest, ForestHill, Plain),
        setOf(TemperateA, TemperateB),
        0.3f, 0.6f, 1f
    ),
    PlantSpawn({ PearTree() },
        setOf(Forest, ForestHill),
        setOf(TemperateA, TemperateB, TropicalA, TropicalB),
        0.3f, 0.5f, 1f
    ),
    PlantSpawn({ PineTree() },
        setOf(Forest, ForestHill, Mountain, Hill),
        setOf(TemperateA, TemperateB, AlpineA, AlpineB),
        1f, 0.7f, 1f
    ),
    PlantSpawn({ SpruceTree() },
        setOf(Forest, ForestHill),
        setOf(AlpineA, AlpineB),
        1f, 0.6f, 1f
    ),
    PlantSpawn({ PalmTree() },
        setOf(Desert, Forest, Plain, Swamp),
        setOf(TropicalA, TropicalB),
        1f, 0.7f, 1f
    ),
    PlantSpawn({ CoconutTree() },
        setOf(Forest, ForestHill, Plain, Swamp),
        setOf(TropicalA, TropicalB),
        0.4f, 0.7f, 1f
    ),
    PlantSpawn({ DeadTree() },
        setOf(Mountain, Forest, ForestHill, Hill, Swamp),
        setOf(TemperateA, TemperateB, AlpineA, AlpineB),
        0.3f, 0.6f, 1f
    ),

    // Bushes
    PlantSpawn({ ThornBush() },
        setOf(Plain, Scrub, Hill, Forest, ForestHill, Swamp),
        setOf(TemperateA, TemperateB, AlpineA, AlpineB, TropicalA, TropicalB),
        1f, 0f, 0.8f
    ),
    PlantSpawn({ SageBush() },
        setOf(Plain, Scrub, Desert),
        setOf(TemperateA, TemperateB, AlpineA, AlpineB, TropicalA, TropicalB),
        1f, 0f, 0.8f
    ),
    PlantSpawn({ BerryBush() },
        setOf(Plain, Forest, Swamp),
        setOf(TemperateA, TemperateB, AlpineA, AlpineB, TropicalA, TropicalB),
        1f, 0.3f, 0.8f
    ),
    PlantSpawn({ HoneypodBush() },
        setOf(Hill, ForestHill),
        setOf(TemperateA, TemperateB, TropicalA, TropicalB),
        1f, 0.4f, 0.8f
    ),

    // Flowers
    PlantSpawn({ Wildflowers() },
        setOf(Forest, Plain, Scrub, Hill, ForestHill, Swamp, Desert),
        setOf(TemperateA, TemperateB, AlpineA, AlpineB, TropicalA, TropicalB),
        0.4f, 0f, 0.5f
    ),
    PlantSpawn({ Poppies() },
        setOf(Plain),
        setOf(TemperateA, TemperateB, TropicalA, TropicalB),
        0.1f, 0.2f, 0.5f
    ),
    PlantSpawn({ Deathflower() },
        setOf(Desert, Scrub),
        setOf(TemperateA, TemperateB, TropicalA, TropicalB),
        0.05f, 0.4f, 0.7f
    ),
    PlantSpawn({ Dreamflower() },
        setOf(Desert, Scrub),
        setOf(TemperateA, TemperateB, TropicalA, TropicalB),
        0.02f, 0.2f, 0.6f
    ),
    PlantSpawn({ Sunflower() },
        setOf(Plain, Scrub),
        setOf(TemperateA, TropicalA),
        0.05f, 0.2f, 0.6f
    ),
    PlantSpawn({ Lightflower() },
        setOf(Desert, Scrub),
        setOf(TemperateB, TropicalA, TropicalB),
        0.1f, 0.4f, 0.7f
    ),

    // Cacti
    PlantSpawn({ Saguaro() },
        setOf(Desert),
        setOf(TemperateA, TemperateB, AlpineA, AlpineB, TropicalA, TropicalB),
        1f, 0.7f, 1f
    ),
    PlantSpawn({ Cholla() },
        setOf(Desert, Scrub),
        setOf(TemperateA, TemperateB, AlpineA, AlpineB, TropicalA, TropicalB),
        1f, 0.5f, 1f
    ),
    PlantSpawn({ Prickpear() },
        setOf(Desert),
        setOf(TemperateA, TemperateB, AlpineA, AlpineB, TropicalA, TropicalB),
        0.3f, 0.5f, 1f
    ),

    // Oddballs
    PlantSpawn({ BalmMoss() },
        setOf(Forest, ForestHill),
        setOf(TemperateA, TemperateB, AlpineA, AlpineB),
        0.1f, 0.5f, 0.8f
    ),
    PlantSpawn({ LaceMoss() },
        setOf(Forest, ForestHill, Swamp),
        setOf(TemperateA, TemperateB, TropicalA, TropicalB),
        0.2f, 0.4f, 0.8f
    ),
    PlantSpawn({ Foolsleaf() },
        setOf(Swamp),
        setOf(TemperateA, TemperateB, TropicalA, TropicalB),
        0.2f, 0.5f, 1f
    ),
    PlantSpawn({ Boulder() },
        setOf(Desert, Cavern, Mountain),
        setOf(TemperateA, TemperateB, AlpineA, AlpineB, TropicalA, TropicalB),
        0.1f, 0f, 0.3f
    ),

    // Mushrooms
    PlantSpawn({ WizardcapMushroom() },
        setOf(Hill, Mountain, Cavern),
        setOf(TemperateA, TemperateB, AlpineA, AlpineB),
        0.03f, 0.6f, 1f
    ),
    PlantSpawn({ SpeckledMushroom() },
        setOf(ForestHill, Cavern),
        setOf(TemperateA, TemperateB),
        0.1f, 0.6f, 1f
    ),
    PlantSpawn({ BloodcapMushroom() },
        setOf(ForestHill),
        setOf(TemperateA, TemperateB, TropicalA, TropicalB),
        0.05f, 0f, 0.2f
    )
)

fun gardenPlantSpawns() = listOf<PlantSpawn>(
    PlantSpawn({ Wildflowers() },
    setOf(Forest, ForestHill, Swamp, Desert, Plain, Scrub, Mountain, Hill),
        setOf(TemperateA, TemperateB, AlpineA, AlpineB, TropicalA, TropicalB),
        2f, 0f, 1f
    ),
    PlantSpawn({ Poppies() },
        setOf(Plain),
        setOf(TemperateA, TemperateB, TropicalA, TropicalB),
        2f,  0f, 1f
    ),
    PlantSpawn({ Deathflower() },
        setOf(Desert, Scrub),
        setOf(TemperateA, TemperateB, TropicalA, TropicalB),
        0.1f,  0f, 1f
    ),
    PlantSpawn({ Dreamflower() },
        setOf(Desert, Scrub),
        setOf(TemperateA, TemperateB, TropicalA, TropicalB),
        0.1f,  0f, 1f
    ),
    PlantSpawn({ Sunflower() },
        setOf(Plain, Scrub),
        setOf(TemperateA, TropicalA),
        0.2f,  0f, 1f
    ),
    PlantSpawn({ Lightflower() },
        setOf(Desert, Scrub),
        setOf(TemperateB, TropicalA, TropicalB),
        0.2f,  0f, 1f
    ),
    PlantSpawn({ BerryBush() },
        setOf(Plain, Forest, Swamp),
        setOf(TemperateA, TemperateB, AlpineA, AlpineB, TropicalA, TropicalB),
        1f,  0f, 1f
    ),
    PlantSpawn({ HoneypodBush() },
        setOf(Hill, ForestHill),
        setOf(TemperateA, TemperateB, TropicalA, TropicalB),
        1f,  0f, 1f
    ),
    PlantSpawn({ Cholla() },
        setOf(Desert, Scrub),
        setOf(TemperateA, TemperateB, AlpineA, AlpineB, TropicalA, TropicalB),
        1f,  0f, 1f
    ),
    PlantSpawn({ Prickpear() },
        setOf(Desert),
        setOf(TemperateA, TemperateB, AlpineA, AlpineB, TropicalA, TropicalB),
        0.3f,  0f, 1f
    ),
    PlantSpawn({ Boulder() },
        setOf(Desert, Cavern, Mountain),
        setOf(TemperateA, TemperateB, AlpineA, AlpineB, TropicalA, TropicalB),
        0.5f,  0f, 1f
    ),
)
