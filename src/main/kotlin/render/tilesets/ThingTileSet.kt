package render.tilesets

import com.badlogic.gdx.graphics.Texture
import render.Screen
import render.tileholders.AnimatedTile
import render.tileholders.SimpleTile
import render.tileholders.TableTile
import render.tileholders.VariantsTile

fun ThingTileSet() =
    TileSet(SpriteSheets.Sheet.ThingSprites).apply {

        setTile(Glyph.BLANK, SimpleTile(this, 4, 2))
        setTile(Glyph.TREE, VariantsTile(this).apply {
            add(0.4f, 0, 1)
            add(0.3f, 0, 0)
            add(0.3f, 1, 1)
        })

        setTile(Glyph.LIGHTBULB, SimpleTile(this, 1, 0))

        setTile(Glyph.AXE, SimpleTile(this, 2, 0))
        setTile(Glyph.SIGN, VariantsTile(this).apply {
            add(0.5f, 3, 0)
            add(0.5f, 4, 1)
        })
        setTile(Glyph.CHEST, SimpleTile(this, 4, 0))
        setTile(Glyph.CACTUS, VariantsTile(this).apply {
            add(0.5f, 5, 0)
            add(0.5f, 5, 1)
        })
        setTile(Glyph.TOMBSTONE, VariantsTile(this).apply {
            add(0.5f, 2, 1)
            add(0.5f, 2, 2)
        })
        setTile(Glyph.DEAD_TREE, VariantsTile(this).apply {
            add(0.5f, 0, 2)
            add(0.5f, 1, 2)
        })
        setTile(Glyph.BIG_ROCK, VariantsTile(this).apply {
            add(0.5f, 2, 2)
            add(0.5f, 2, 3)
        })
        setTile(Glyph.BLADE, SimpleTile(this, 4, 2))
        setTile(Glyph.HILT, SimpleTile(this, 5, 2))
        setTile(Glyph.HILT_LIT, SimpleTile(this, 5, 4))
        setTile(Glyph.PINE_TREE, VariantsTile(this).apply {
            add(0.5f, 0, 3)
            add(0.5f, 1, 3)
        })
        setTile(Glyph.PALM_TREE, VariantsTile(this).apply {
            add(0.5f, 2, 3)
            add(0.5f, 3, 3)
        })
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
        setTile(Glyph.TABLE, TableTile(this, "table").apply {
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
        setTile(Glyph.CEILING_LIGHT, SimpleTile(this, 4, 3))
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
    }
