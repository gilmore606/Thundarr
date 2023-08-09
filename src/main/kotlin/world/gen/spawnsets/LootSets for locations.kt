package world.gen.spawnsets

import things.Thing
import things.Thing.Tag.*

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

// Found in ruined building chests
object RuinLoot {
    val set = LootSet().apply {
        add(1f, WeaponLoot.set)
        add(1f, BookLoot.set)
        add(1f, ClothingLoot.set)
    }
}
