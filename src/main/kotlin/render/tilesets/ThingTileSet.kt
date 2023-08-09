package render.tilesets

import render.tileholders.AnimatedTile
import render.tileholders.SimpleTile
import render.tileholders.TableTile
import render.tileholders.VariantsTile
import things.Thing

fun ThingTileSet() =
    TileSet(SpriteSheets.Sheet.ThingSprites).apply {

        setTile(Glyph.BLANK, SimpleTile(this, 4, 2))

        setTile(Glyph.OAK_TREE, SimpleTile(this, 0, 0))
        setTile(Glyph.MAPLE_TREE, SimpleTile(this, 0, 1))
        setTile(Glyph.BIRCH_TREE, SimpleTile(this, 1, 1))
        setTile(Glyph.FRUIT_TREE, SimpleTile(this, 10, 0))
        setTile(Glyph.DEAD_TREE, SimpleTile(this, 0, 2))
        setTile(Glyph.PINE_TREE, SimpleTile(this, 0, 3))
        setTile(Glyph.PALM_TREE, SimpleTile(this, 2, 3))

        setTile(Glyph.BUSH_1, SimpleTile(this, 1, 2))
        setTile(Glyph.BUSH_1_FRUIT, SimpleTile(this, 2, 2))
        setTile(Glyph.BUSH_2, SimpleTile(this, 1, 3))
        setTile(Glyph.BUSH_2_FRUIT, SimpleTile(this, 3, 2))
        setTile(Glyph.SUNFLOWER, SimpleTile(this, 4, 2))
        setTile(Glyph.HANGFLOWER, SimpleTile(this, 3, 3))
        setTile(Glyph.SUCCULENT, SimpleTile(this, 4, 3))
        setTile(Glyph.FLOWERS, SimpleTile(this, 4, 10))
        setTile(Glyph.TOADSTOOLS, SimpleTile(this, 5, 10))
        setTile(Glyph.MUSHROOM, SimpleTile(this, 6, 10))
        setTile(Glyph.CACTUS_SMALL, SimpleTile(this, 5, 1))
        setTile(Glyph.CACTUS_BIG, SimpleTile(this, 5, 0))
        setTile(Glyph.HERB_PLANT_1, SimpleTile(this, 7, 10))

        setTile(Glyph.LIGHTBULB, SimpleTile(this, 1, 0))

        setTile(Glyph.AXE, SimpleTile(this, 2, 0))
        setTile(Glyph.TRAIL_SIGN, VariantsTile(this).apply {
            add(0.5f, 3, 0)
            add(0.5f, 4, 1)
        })
        setTile(Glyph.CHEST, SimpleTile(this, 4, 0))
        setTile(Glyph.GRAVESTONE, VariantsTile(this).apply {
            add(0.5f, 2, 1)
            add(0.5f, 3, 1)
        })
        setTile(Glyph.SUNSWORD, SimpleTile(this, 5, 2))
        setTile(Glyph.SUNSWORD_LIT, SimpleTile(this, 5, 4))
        setTile(Glyph.BOTTLE, SimpleTile(this, 5, 3))
        setTile(Glyph.FRUIT, SimpleTile(this, 0, 4))
        setTile(Glyph.TORCH, SimpleTile(this, 1, 4))
        setTile(Glyph.TORCH_LIT, AnimatedTile(this).apply {
            add(2, 4)
            add(2, 5)
        })
        setTile(Glyph.SMOKE_PUFF, SimpleTile(this, 0, 5))
        setTile(Glyph.DUST_PUFF, SimpleTile(this, 1, 5))
        setTile(Glyph.BLOODSTAIN, VariantsTile(this).apply {
            add(0.5f, 3, 4)
            add(0.5f, 4, 4)
        })
        setTile(Glyph.SCORCHMARK, SimpleTile(this, 7, 5))
        setTile(Glyph.CORPSE, SimpleTile(this, 3, 5))
        setTile(Glyph.GOO_CORPSE, SimpleTile(this, 12, 3))
        setTile(Glyph.MEAT, SimpleTile(this, 4, 5))
        setTile(Glyph.FILING_CABINET, SimpleTile(this, 5, 5))
        setTile(Glyph.HARD_HAT, SimpleTile(this, 6, 0))
        setTile(Glyph.HORNED_HAT, SimpleTile(this, 6, 1))
        setTile(Glyph.HELMET, SimpleTile(this, 6, 2))
        setTile(Glyph.LOG, SimpleTile(this, 6, 3))
        setTile(Glyph.BRICK, SimpleTile(this, 6, 4))
        setTile(Glyph.RAINDROP, AnimatedTile(this).apply {
            frameMs = 26
            add(0, 6)
            add(1, 6)
            add(2, 6)
            add(3, 6)
            add(4, 6)
            add(5, 6)
            add(6, 6)
            add(7, 6)
        })
        setTile(Glyph.LIGHTER, SimpleTile(this, 6, 5))
        setTile(Glyph.CAMPFIRE, SimpleTile(this, 7, 4))
        setTile(Glyph.DOOR_CLOSED, SimpleTile(this, 7, 0))
        setTile(Glyph.DOOR_OPEN, SimpleTile(this, 7, 1))
        setTile(Glyph.TABLE, TableTile(this, Thing.Tag.TABLE).apply {
            add(TableTile.Slot.SINGLE, 7, 2)
            add(TableTile.Slot.MIDDLE, 9, 2)
            add(TableTile.Slot.LEFT, 8, 1)
            add(TableTile.Slot.RIGHT, 9, 1)
            add(TableTile.Slot.UPPER, 8, 0)
        })
        setTile(Glyph.BOARD, SimpleTile(this, 8, 2))
        setTile(Glyph.GRAY_BOOTS, SimpleTile(this, 9, 3))
        setTile(Glyph.GRAY_BOOTS_WORN, SimpleTile(this, 8, 3))
        setTile(Glyph.BROWN_BOOTS, SimpleTile(this, 9, 5))
        setTile(Glyph.BROWN_BOOTS_WORN, SimpleTile(this, 8, 5))
        setTile(Glyph.RED_TUNIC, SimpleTile(this, 9, 4))
        setTile(Glyph.RED_TUNIC_WORN, SimpleTile(this, 8, 4))
        setTile(Glyph.RED_JACKET, SimpleTile(this, 9, 6))
        setTile(Glyph.RED_JACKET_WORN, SimpleTile(this, 8, 6))
        setTile(Glyph.NECK_CHARM, SimpleTile(this, 9, 7))
        setTile(Glyph.NECK_CHARM_WORN, SimpleTile(this, 8, 7))
        setTile(Glyph.CEILING_LIGHT, SimpleTile(this, 9, 9))
        setTile(Glyph.CLOTH_ROLL, SimpleTile(this, 0, 7))
        setTile(Glyph.MEDPACK, SimpleTile(this, 1, 7))
        setTile(Glyph.PILL_BOTTLE, SimpleTile(this, 2, 7))
        setTile(Glyph.PACKAGE_BAG, SimpleTile(this, 3, 7))
        setTile(Glyph.BATTERY, SimpleTile(this, 4, 7))
        setTile(Glyph.FLASHLIGHT, SimpleTile(this, 5, 7))
        setTile(Glyph.COMPUTER_OFF, SimpleTile(this, 6, 7))
        setTile(Glyph.COMPUTER_ON, SimpleTile(this, 7, 7))
        setTile(Glyph.STRONGBOX, SimpleTile(this, 0, 8))
        setTile(Glyph.PORTABLE_DEVICE_1, SimpleTile(this, 1, 8))
        setTile(Glyph.PORTABLE_DEVICE_2, SimpleTile(this, 2, 8))
        setTile(Glyph.FOOD_CAN, SimpleTile(this, 3, 8))
        setTile(Glyph.JEWELED_RING, SimpleTile(this, 4, 8))
        setTile(Glyph.CHICKEN_LEG, SimpleTile(this, 5, 8))
        setTile(Glyph.CHEESE, SimpleTile(this, 6, 8))
        setTile(Glyph.JAR, SimpleTile(this, 7, 8))
        setTile(Glyph.BOOK_GRAY, SimpleTile(this, 8, 8))
        setTile(Glyph.BOOK_PURPLE, SimpleTile(this, 9, 8))
        setTile(Glyph.KEY, SimpleTile(this, 0, 9))
        setTile(Glyph.STEW, SimpleTile(this, 1, 9))
        setTile(Glyph.BEDROLL, SimpleTile(this, 2, 9))
        setTile(Glyph.FRIDGE, SimpleTile(this, 3, 9))
        setTile(Glyph.GRAY_TUNIC_WORN, SimpleTile(this, 4, 9))
        setTile(Glyph.GRAY_TUNIC, SimpleTile(this, 5, 9))
        setTile(Glyph.STOVE, SimpleTile(this, 6, 9))
        setTile(Glyph.SLINGSHOT, SimpleTile(this, 7, 9))
        setTile(Glyph.ROCK, SimpleTile(this, 8, 9))
        setTile(Glyph.ROCK_WORN, SimpleTile(this, 12, 6))
        setTile(Glyph.WOOD_DOOR_CLOSED, SimpleTile(this, 0, 10))
        setTile(Glyph.WOOD_DOOR_OPEN, SimpleTile(this, 1, 10))
        setTile(Glyph.TRIDENT, SimpleTile(this, 2, 10))
        setTile(Glyph.SPEAR, SimpleTile(this, 3, 10))
        setTile(Glyph.HIGHWAY_SIGN, SimpleTile(this, 9, 10))
        setTile(Glyph.GLOWING_CRYSTAL, SimpleTile(this, 10, 1))
        setTile(Glyph.BONEPILE, SimpleTile(this, 10, 2))
        setTile(Glyph.BOULDER, SimpleTile(this, 10, 3))
        setTile(Glyph.WRECKED_CAR, VariantsTile(this).apply {
            add(0.5f, 10, 4)
            add(0.5f, 10, 5)
        })
        setTile(Glyph.WELL, SimpleTile(this, 10, 6))
        setTile(Glyph.SHRINE, SimpleTile(this, 10, 7))
        setTile(Glyph.BOOKSHELF, SimpleTile(this, 10, 8))
        setTile(Glyph.FORGE, SimpleTile(this, 10, 9))
        setTile(Glyph.WARDROBE, SimpleTile(this, 10, 10))
        setTile(Glyph.RAILS_V, SimpleTile(this, 11, 0))
        setTile(Glyph.RAILS_H, SimpleTile(this, 12, 0))
        setTile(Glyph.LEATHER, SimpleTile(this, 11, 1))
        setTile(Glyph.CANDLESTICK_OFF, SimpleTile(this, 11, 2))
        setTile(Glyph.CANDLESTICK_ON, SimpleTile(this, 11, 3))
        setTile(Glyph.LAMPPOST_OFF, SimpleTile(this, 11, 4))
        setTile(Glyph.LAMPPOST_ON, SimpleTile(this, 11, 5))
        setTile(Glyph.STICK, SimpleTile(this, 7, 3))
        setTile(Glyph.BED, SimpleTile(this, 11, 6))
        setTile(Glyph.LANTERN, SimpleTile(this, 8, 10))
        setTile(Glyph.HAMMER, SimpleTile(this, 9, 9))
        setTile(Glyph.KNIFE_WORN, SimpleTile(this, 11, 7))
        setTile(Glyph.KNIFE, SimpleTile(this, 12, 7))
        setTile(Glyph.SWORD_WORN, SimpleTile(this, 11, 8))
        setTile(Glyph.SWORD, SimpleTile(this, 12, 8))
        setTile(Glyph.CLOAK_WORN, SimpleTile(this, 11, 9))
        setTile(Glyph.CLOAK, SimpleTile(this, 12, 9))
        setTile(Glyph.RED_PANTS_WORN, SimpleTile(this, 11, 10))
        setTile(Glyph.RED_PANTS, SimpleTile(this, 12, 10))
        setTile(Glyph.CANDLE_OFF, SimpleTile(this, 12, 1))
        setTile(Glyph.CANDLE_ON, SimpleTile(this, 12, 2))
        setTile(Glyph.YELLOW_PROJECTILE, SimpleTile(this, 12, 4))
        setTile(Glyph.GOLD_TREASURE, SimpleTile(this, 12, 5))
    }
