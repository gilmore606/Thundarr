package world.gen.spawnsets

import things.*


// For sale in village smithy
object SmithyWares {
    val set = LootSet().apply {
        add(1f, Thing.Tag.HAMMER, Hammer.sellVariants)
        add(1f, Thing.Tag.KNIFE, Knife.sellVariants)
        add(1f, Thing.Tag.AXE, Axe.sellVariants)
        add(1f, Thing.Tag.PICKAXE, Pickaxe.sellVariants)
        add(1f, Thing.Tag.PITCHFORK, Pitchfork.sellVariants)
        add(1f, Thing.Tag.CLUB, Club.sellVariants)
        add(1f, Thing.Tag.GLADIUS, Gladius.sellVariants)
        add(1f, Thing.Tag.LONGSWORD, Longsword.sellVariants)
        add(1f, Thing.Tag.WARHAMMER, Warhammer.sellVariants)
        add(1f, Thing.Tag.BRONZE_SPEAR, BronzeSpear.sellVariants)
        add(1f, Thing.Tag.STEEL_SPEAR, SteelSpear.sellVariants)
    }
}

// For sale in taverns
object TavernWares {
    val set = LootSet().apply {
        add(1f, Thing.Tag.STEW)
        add(1f, Thing.Tag.STEAK)
        add(1f, Thing.Tag.CHICKENLEG)
        add(1f, Thing.Tag.ENERGYDRINK)
        add(0.5f, Thing.Tag.THRALLCHOW)
    }
}

// For sale in general stores
object GeneralStoreWares {
    val set = LootSet().apply {
        add(3f, ClothingLoot.set)
        add(1f, Thing.Tag.TORCH)
        add(1f, Thing.Tag.LIGHTER, 2)
        add(1f, Thing.Tag.BANDAGES)
        add(1f, Thing.Tag.CANDLE)
    }
}
