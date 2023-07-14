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

object SkeletonLoot {
    val set = LootSet().apply {
        add(1f, WeaponLoot.set)
        add(1f, BookLoot.set)
    }
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
