package world.gen.spawnsets

import things.*
import things.Thing.Tag.*
import things.gearmods.GearMod
import things.gearmods.GearMod.Tag.*
import util.Dice
import util.total
import world.gen.spawnsets.LootSet.*

open class LootSet {

    sealed class Entry(
        val freq: Float,
        val limit: Pair<Int,Int> = Pair(0,100)
    )

    open class ItemEntry(
        freq: Float,
        val item: Thing.Tag,
        limit: Pair<Int,Int> = Pair(0,100),
        val mod: GearMod.Tag? = null,
        val customizer: ((Thing)->Unit)? = null
    ) : Entry(freq, limit)

    class SubsetEntry(
        freq: Float,
        val subset: LootSet,
        limit: Pair<Int,Int> = Pair(0,100)
    ) : Entry(freq, limit)

    class Variant(
        val freq: Float,
        val mod: GearMod.Tag? = null,
        val levelMin: Int = 0,
        val levelMax: Int = 100,
        val customizer: ((Thing)->Unit)? = null
    )

    val set: MutableSet<Entry> = mutableSetOf()

    fun add(freq: Float, item: Thing.Tag, levelMin: Int = 0, levelMax: Int = 100, mod: GearMod.Tag? = null, custom: ((Thing)->Unit)? = null) {
        set.add(ItemEntry(
            freq = freq, item = item, limit = Pair(levelMin, levelMax), mod = mod, customizer = custom)
        )
    }

    fun add(freq: Float, subset: LootSet, levelMin: Int = 0, levelMax: Int = 100) {
        set.add(SubsetEntry(
            freq = freq, subset = subset, limit = Pair(levelMin, levelMax)
        ))
    }

    fun add(freq: Float, item: Thing.Tag, variants: List<Variant>) {
        val freqTotal = variants.total { it.freq }
        variants.forEach {
            add(freq * (it.freq / freqTotal), item, it.levelMin, it.levelMax, it.mod, it.customizer)
        }
    }

    fun getLoot(level: Int): Thing? {
        val filtered = mutableListOf<Pair<Float, Entry>>()
        var freqTotal = 0f
        set.forEach { cand ->
            when (level) {
                cand.limit.first - 1 -> cand.freq * 0.5f
                cand.limit.first - 2 -> cand.freq * 0.25f
                cand.limit.second + 1 -> cand.freq * 0.25f
                in cand.limit.first..cand.limit.second -> cand.freq
                else -> null
            }?.also { freq ->
                filtered.add(Pair(freq, cand))
                freqTotal += freq
            }
        }
        if (filtered.isEmpty()) return null

        val roll = Dice.float(0f, freqTotal)
        var acc = 0f
        var winner: Entry? = null
        filtered.forEach {
            if (winner == null) {
                acc += it.first
                if (roll <= acc) {
                    winner = it.second
                }
            }
        }
        if (winner is SubsetEntry) {
            return (winner as SubsetEntry).subset.getLoot(level)
        } else if (winner is ItemEntry) {
            (winner as ItemEntry).also {
                val thing = it.item.create()
                it.mod?.also { (thing as Gear).addMod(it.get) }
                it.customizer?.invoke(thing)
                return thing
            }
        }
        return null
    }
}

object WeaponLoot {
    val set = LootSet().apply {
        add(1f, STONE_AXE, 0, 2)
        add(1f, KNIFE, Knife.variants)
        add(1f, AXE, Axe.variants)
        add(1f, PICKAXE, Pickaxe.variants)
        add(1f, CLUB, Club.variants)
        add(1f, GLADIUS, Gladius.variants)
        add(1f, LONGSWORD, Longsword.variants)
        add(1f, WARHAMMER, Warhammer.variants)
        add(1f, BRONZE_SPEAR, BronzeSpear.variants)
        add(0.7f, STEEL_SPEAR, SteelSpear.variants)
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
    val toolVariants = listOf(
        Variant(1f, RUSTY, 1, 3),
        Variant(1f, BENT, 1, 3),
        Variant(2f, null, 2),
        Variant(1f, LIGHT, 2,),
        Variant(1f, HEAVY, 2,),
        Variant(1f, FINE, 3),
        Variant(0.5f, MASTER, 4),
    )

    val set = LootSet().apply {
        add(1f, HAMMER, toolVariants)
        add(1f, KNIFE, toolVariants)
        add(1f, STONE_AXE)
        add(1f, AXE, toolVariants)
        add(1f, PICKAXE, toolVariants)
        add(1f, PITCHFORK, toolVariants)
        add(0.1f, LIGHTER, 2)
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
        add(1f, HAMMER, Hammer.sellVariants)
        add(1f, KNIFE, Knife.sellVariants)
        add(1f, AXE, Axe.sellVariants)
        add(1f, PICKAXE, Pickaxe.sellVariants)
        add(1f, PITCHFORK, Pitchfork.sellVariants)
        add(1f, CLUB, Club.sellVariants)
        add(1f, GLADIUS, Gladius.sellVariants)
        add(1f, LONGSWORD, Longsword.sellVariants)
        add(1f, WARHAMMER, Warhammer.sellVariants)
        add(1f, BRONZE_SPEAR, BronzeSpear.sellVariants)
        add(1f, STEEL_SPEAR, SteelSpear.sellVariants)
    }
}

// For sale in taverns
object TavernWares {
    val set = LootSet().apply {
        add(1f, STEW)
        add(1f, STEAK)
        add(1f, CHICKENLEG)
        add(1f, ENERGYDRINK)
        add(0.5f, THRALLCHOW)
    }
}

// For sale in general stores
object GeneralStoreWares {
    val set = LootSet().apply {
        add(3f, ClothingLoot.set)
        add(1f, TORCH)
        add(1f, LIGHTER, 2)
        add(1f, BANDAGES)
        add(1f, CANDLE)
    }
}
