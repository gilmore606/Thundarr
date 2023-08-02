package world.gen.spawnsets

import things.Thing
import things.Thing.Tag.*

class LootSet : SpawnSet<Thing.Tag, Thing, Any>() {

    fun add(freq: Float, item: Thing.Tag, levelMin: Int = 0, levelMax: Int = 100) {
        addEntry(freq, item, limit = Pair(levelMin, levelMax))
    }

    fun add(freq: Float, subset: LootSet, levelMin: Int = 0, levelMax: Int = 100) {
        set.add(SubsetEntry(freq, subset, limit = Pair(levelMin, levelMax)))
    }

    fun getLoot(level: Int) = roll(level)

}

object WeaponLoot {
    val set = LootSet().apply {
        add(1f, STONE_AXE, 0, 2)
        add(1f, AXE, 1, 5)
        add(1f, PICKAXE, 1, 5)
    }
}

object BookLoot {
    val set = LootSet().apply {
        add(1f, PAPERBACK)
        add(1f, BOYSLIFE)
    }
}

object ClothingLoot {
    val set = LootSet().apply {
        add(1f, SHOES)
        add(1f, TRAVEL_CLOAK)
        add(1f, TRAVEL_BOOTS)
        add(1f, WOOL_HAT)
        add(1f, JEANS)
        add(1f, LEATHER_PANTS)
        add(1f, FUR_PANTS, 2)
        add(1f, FUR_TUNIC, 2)
        add(1f, LEATHER_VEST)
        add(1f, SCALE_VEST, 2)
        add(1f, LEATHER_JACKET, 2)
        add(1f, SCALE_JACKET, 3)
        add(1f, FUR_JACKET, 3)
    }
}

object FarmToolsLoot {
    val set = LootSet().apply {
        add(1f, HAMMER)
        add(1f, KNIFE)
        add(1f, STONE_AXE)
        add(1f, AXE, 2)
        add(1f, PICKAXE, 2)
        add(1f, PITCHFORK, 2)
        add(1f, LIGHTER, 3)
        add(0.5f, CANDLE)
        add(2f, TORCH)
    }
}

object FoodLoot {
    val set = LootSet().apply {
        add(1f, CHEESE)
        add(1f, APPLE)
        add(1f, PEAR)
        add(1f, CHICKENLEG)
    }
}

// Found on travelling folk
object TravelerLoot {
    val set = LootSet().apply {
        add(1f, WeaponLoot.set)
        add(0.5f, BookLoot.set)
        add(1f, ClothingLoot.set)
    }
}

// Found in barns and farms
object BarnLoot {
    val set = LootSet().apply {
        add(1f, FarmToolsLoot.set)
        add(0.4f, ClothingLoot.set)
        add(0.1f, BookLoot.set)
    }
}

// Found in village huts
object HutLoot {
    val set = LootSet().apply {
        add(1f, ClothingLoot.set)
        add(1f, FoodLoot.set)
        add(0.5f, FarmToolsLoot.set)
        add(1f, BookLoot.set)
    }
}

// Found in shrines
object ShrineLoot {
    val set = LootSet().apply {
        add(1f, CANDLE)
        add(0.1f, LIGHTER)
        add(0.2f, KNIFE)
    }
}

// For sale in village smithy
object SmithyWares {
    val set = LootSet().apply {
        add(1f, HAMMER)
        add(1f, KNIFE)
        add(1f, STONE_AXE)
        add(1f, AXE,)
        add(1f, PICKAXE,)
        add(1f, PITCHFORK,)
        add(1f, GLADIUS)
    }
}

// For sale in taverns
object TavernWares {
    val set = LootSet().apply {
        add(1f, STEW)
        add(1f, STEAK)
        add(1f, CHICKENLEG)
        add(1f, ENERGYDRINK)
    }
}
