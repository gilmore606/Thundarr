package world.gen.spawnsets

import things.*
import things.Thing.Tag.*
import things.gearmods.GearMod.Tag.*
import util.Dice


object TreasureLoot {
    class TreasureLootSet : LootSet() {
        override fun getLoot(level: Int): Thing? {
            val worth = level / 2
            if (Dice.chance(0.05f)) return Treasure(worth + 5)
            return Treasure(Dice.range(worth - 2, worth + 3))
        }
    }
    val set = TreasureLootSet()
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
        LootSet.Variant(1f, RUSTY, 1, 3),
        LootSet.Variant(1f, BENT, 1, 3),
        LootSet.Variant(2f, null, 2),
        LootSet.Variant(1f, LIGHT, 2,),
        LootSet.Variant(1f, HEAVY, 2,),
        LootSet.Variant(1f, FINE, 3),
        LootSet.Variant(0.5f, MASTER, 4),
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
        add(0.5f, THRALLCHOW)
    }
}
