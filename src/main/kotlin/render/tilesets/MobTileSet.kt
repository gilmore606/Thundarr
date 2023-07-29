package render.tilesets

import render.tileholders.AnimatedTile
import render.tileholders.SimpleTile

fun ActorTileSet() =
    TileSet(SpriteSheets.Sheet.ActorSprites).apply {

        setTile(Glyph.MOB_SHADOW, SimpleTile(this, 3, 1))
        setTile(Glyph.MOB_WATER_SHADOW, SimpleTile(this, 3, 0))

        setTile(Glyph.PLAYER, SimpleTile(this, 0, 1))
        setTile(Glyph.ARIEL, SimpleTile(this, 0, 0))
        setTile(Glyph.MOK, SimpleTile(this, 2, 1))
        setTile(Glyph.CATTLE, SimpleTile(this, 3, 2))
        setTile(Glyph.CACTOID, SimpleTile(this, 0, 2))
        setTile(Glyph.HYENAMAN, SimpleTile(this, 1, 2))
        setTile(Glyph.GIANT_ANT, SimpleTile(this, 2, 2))
        setTile(Glyph.DROID_1, SimpleTile(this, 1, 0))
        setTile(Glyph.DROID_2, SimpleTile(this, 2, 0))
        setTile(Glyph.WOLFMAN, SimpleTile(this, 0, 3))
        setTile(Glyph.SNAKEMAN, SimpleTile(this, 1, 3))
        setTile(Glyph.KARROK, SimpleTile(this, 2, 3))
        setTile(Glyph.THRALL, SimpleTile(this, 3, 3))
        setTile(Glyph.RATMAN, SimpleTile(this, 0, 4))
        setTile(Glyph.RATTHING, SimpleTile(this, 1, 4))
        setTile(Glyph.RATLORD, SimpleTile(this, 3, 4))
        setTile(Glyph.FLOATING_EYE, SimpleTile(this, 2, 4))
        setTile(Glyph.WIZARD_SCYTHE, SimpleTile(this, 4, 0))
        setTile(Glyph.SHIELD_GUARD, SimpleTile(this, 4, 2))
        setTile(Glyph.THRALL_ARCHER, SimpleTile(this, 4, 1))
        setTile(Glyph.FURWORM, SimpleTile(this, 4, 3))
        setTile(Glyph.PIG, SimpleTile(this, 4, 4))
        setTile(Glyph.VOLTELOPE, SimpleTile(this, 0, 5))
        setTile(Glyph.DEMONDOG, SimpleTile(this, 1, 5))
        setTile(Glyph.LANDWHALE, SimpleTile(this, 5, 4))
        setTile(Glyph.TAILMANDER, SimpleTile(this, 5, 3))
        setTile(Glyph.CYCLOX, SimpleTile(this, 6, 3))
        setTile(Glyph.TORTLE, SimpleTile(this, 5, 0))
        setTile(Glyph.PIDGEY, SimpleTile(this, 6, 2))
        setTile(Glyph.PIDGEY_BRUTE, SimpleTile(this, 6, 0))
        setTile(Glyph.BLUE_BIRD, AnimatedTile(this).apply {
            frameMs = 381
            add(5, 5)
            add(6, 5)
        })
        setTile(Glyph.GOAT, SimpleTile(this, 2, 5))
        setTile(Glyph.MANAPE, SimpleTile(this, 3, 5))
        setTile(Glyph.GATOR, SimpleTile(this, 6, 4))
        setTile(Glyph.PEASANT_PALE_DARK, SimpleTile(this, 7, 0))
        setTile(Glyph.PEASANT_PALE_RED, SimpleTile(this, 7, 1))
        setTile(Glyph.PEASANT_PALE_BLOND, SimpleTile(this, 7, 2))
        setTile(Glyph.PEASANT_PALE_GREEN, SimpleTile(this, 7, 3))
        setTile(Glyph.PEASANT_PALE_CHILD, SimpleTile(this, 7, 4))
        setTile(Glyph.PEASANT_TAN_DARK, SimpleTile(this, 8, 0))
        setTile(Glyph.PEASANT_TAN_BLOND, SimpleTile(this, 8, 1))
        setTile(Glyph.PEASANT_TAN_RED, SimpleTile(this, 8, 2))
        setTile(Glyph.PEASANT_TAN_GREEN, SimpleTile(this, 8, 3))
        setTile(Glyph.PEASANT_TAN_CHILD, SimpleTile(this, 8, 4))
        setTile(Glyph.PEASANT_WHITE_RED, SimpleTile(this, 9, 0))
        setTile(Glyph.PEASANT_WHITE_DARK, SimpleTile(this, 9, 1))
        setTile(Glyph.PEASANT_WHITE_GREEN, SimpleTile(this, 9, 2))
        setTile(Glyph.PEASANT_WHITE_BLOND, SimpleTile(this, 9, 3))
        setTile(Glyph.PEASANT_WHITE_CHILD, SimpleTile(this, 9, 4))
        setTile(Glyph.PEASANT_BLACK_DARK, SimpleTile(this, 10, 0))
        setTile(Glyph.PEASANT_BLACK_GREEN, SimpleTile(this, 10, 1))
        setTile(Glyph.PEASANT_BLACK_RED, SimpleTile(this, 10, 2))
        setTile(Glyph.PEASANT_BLACK_BLOND, SimpleTile(this, 10, 3))
        setTile(Glyph.PEASANT_BLACK_CHILD, SimpleTile(this, 10, 4))
        setTile(Glyph.DOG, SimpleTile(this, 1, 6))
        setTile(Glyph.JERIF, SimpleTile(this, 2, 6))
        setTile(Glyph.MAGIC_PORTAL, SimpleTile(this, 3, 6))
        setTile(Glyph.PINCER_BEETLE, SimpleTile(this, 0, 6))
        setTile(Glyph.FROG, SimpleTile(this, 0, 7))
        setTile(Glyph.SPOTTED_BEETLE, SimpleTile(this, 1, 7))
        setTile(Glyph.GECKOID, SimpleTile(this, 4, 6))
        setTile(Glyph.CRAB, SimpleTile(this, 4, 7))
        setTile(Glyph.EYEHOP, SimpleTile(this, 5, 6))
        setTile(Glyph.MANOTAUR, SimpleTile(this, 5, 7))
        setTile(Glyph.LION, SimpleTile(this, 6, 6))
        setTile(Glyph.TOXIE, SimpleTile(this, 6, 7))
        setTile(Glyph.MANT, SimpleTile(this, 0, 8))
        setTile(Glyph.LOCUST, SimpleTile(this, 1, 8))
        setTile(Glyph.SLUG, SimpleTile(this, 2, 8))
        setTile(Glyph.BIG_SPECTER, SimpleTile(this, 3, 9))
        setTile(Glyph.SMALL_SPECTER, SimpleTile(this, 3, 8))
        setTile(Glyph.WOOD_LORD, SimpleTile(this, 4, 8))
        setTile(Glyph.SANDSTRIDER, SimpleTile(this, 7, 5))
        setTile(Glyph.BIG_SLIMER, SimpleTile(this, 5, 8))
        setTile(Glyph.SMALL_SLIMER, SimpleTile(this, 6, 8))
        setTile(Glyph.KILLDAISY, SimpleTile(this, 7, 7))
        setTile(Glyph.PRAIRIE_SQUID, SimpleTile(this, 8, 7))
        setTile(Glyph.CHICKEN, SimpleTile(this, 0, 9))
        setTile(Glyph.CYCLOPS, SimpleTile(this, 0, 10))
        setTile(Glyph.TOAD, SimpleTile(this, 1, 9))
        setTile(Glyph.ETTIN, SimpleTile(this, 1, 10))
        setTile(Glyph.BOAR, SimpleTile(this, 2, 9))
        setTile(Glyph.TUFT, SimpleTile(this, 2, 10))
        setTile(Glyph.STILTBLOB, SimpleTile(this, 3, 10))
        setTile(Glyph.PORCUPINE, SimpleTile(this, 4, 9))
        setTile(Glyph.HYENATAR, SimpleTile(this, 4, 10))
        setTile(Glyph.MUFFALO, SimpleTile(this, 5, 9))
        setTile(Glyph.KARROK_ARCHER, SimpleTile(this, 5, 10))
        setTile(Glyph.WOLFBEAST, SimpleTile(this, 6, 9))
        setTile(Glyph.MERMAN, SimpleTile(this, 6, 10))
        setTile(Glyph.LEECHMAN, SimpleTile(this, 7, 9))
        setTile(Glyph.MERMAGE, SimpleTile(this, 7, 10))
        setTile(Glyph.BRAINBOT, SimpleTile(this, 8, 5))
        setTile(Glyph.COMBOT, SimpleTile(this, 9, 5))
        setTile(Glyph.TANKBOT, SimpleTile(this, 8, 6))
        setTile(Glyph.WIDOW_SPIDER, SimpleTile(this, 8, 7))
        setTile(Glyph.SCORPION, SimpleTile(this, 8, 8))
        setTile(Glyph.FIRE_SLUG, SimpleTile(this, 8, 9))
        setTile(Glyph.GRUB, SimpleTile(this, 8, 10))
        setTile(Glyph.ROCKMAN, SimpleTile(this, 9, 6))
        setTile(Glyph.BROWN_SPIDER, SimpleTile(this, 9, 7))
        setTile(Glyph.FIRE_SPIRIT, SimpleTile(this, 9, 8))
        setTile(Glyph.COPPER_BUG, SimpleTile(this, 9, 9))
        setTile(Glyph.TICK, SimpleTile(this, 9, 10))
        setTile(Glyph.GRANITEMAN, SimpleTile(this, 10, 5))
        setTile(Glyph.DEAD_TREE_MAN, SimpleTile(this, 10, 6))
        setTile(Glyph.TREE_MAN, SimpleTile(this, 10, 7))
        setTile(Glyph.WATER_SPIRIT, SimpleTile(this, 10, 8))
        setTile(Glyph.BRICK_MAN, SimpleTile(this, 10, 9))
        setTile(Glyph.WILLOWISP, SimpleTile(this, 10, 10))
        setTile(Glyph.ARMBOT, SimpleTile(this, 11, 0))
        setTile(Glyph.BIG_DEAD_TREE_MAN, SimpleTile(this, 11, 1))
        setTile(Glyph.CHEM_RAIDER, SimpleTile(this, 12, 0))
        setTile(Glyph.CHEM_GUNNER, SimpleTile(this, 12, 1))
        setTile(Glyph.EELMAN, SimpleTile(this, 12, 2))
        setTile(Glyph.RAD_SPEARMAN, SimpleTile(this, 12, 3))
        setTile(Glyph.RAD_MELEE, SimpleTile(this, 11, 3))
        setTile(Glyph.WITCH, SimpleTile(this, 12, 4))
        setTile(Glyph.GASBAG, SimpleTile(this, 11, 4))
        setTile(Glyph.ROBO_BOSS, SimpleTile(this, 12, 5))
        setTile(Glyph.GUNLUGGER, SimpleTile(this, 11, 5))
        setTile(Glyph.GUNSLINGER, SimpleTile(this, 11, 6))
        setTile(Glyph.SAND_BANDIT, SimpleTile(this, 12, 7))
        setTile(Glyph.WHITE_DRAKE, SimpleTile(this, 11, 7))
        setTile(Glyph.RED_DRAKE, SimpleTile(this, 11, 8))
        setTile(Glyph.GREEN_DRAKE, SimpleTile(this, 12, 8))
        setTile(Glyph.LAMPREY, SimpleTile(this, 12, 9))
        setTile(Glyph.FIRE_TORNADO, SimpleTile(this, 11, 9))
        setTile(Glyph.BEE, SimpleTile(this, 12, 10))
        setTile(Glyph.CORAL_SNAKE, SimpleTile(this, 11, 10))
        setTile(Glyph.FLYDRAGON, SimpleTile(this, 0, 11))
        setTile(Glyph.JELLYBULB, SimpleTile(this, 1, 11))
        setTile(Glyph.SUPERBRUTE, SimpleTile(this, 2, 11))
        setTile(Glyph.LOSTRICH, SimpleTile(this, 3, 11))
        setTile(Glyph.TRASH_MONSTER, SimpleTile(this, 4, 11))
        setTile(Glyph.MANTIS_MAN, SimpleTile(this, 5, 11))
        setTile(Glyph.DEVIL_BULL, SimpleTile(this, 6, 11))
        setTile(Glyph.WASTED_ONE, SimpleTile(this, 7, 11))
        setTile(Glyph.CONSTRUCT_WITCH, SimpleTile(this, 8, 11))
        setTile(Glyph.CONSTRUCT_STRIKER, SimpleTile(this, 9, 11))
        setTile(Glyph.ENERGY_SPHERE, SimpleTile(this, 10, 11))
        setTile(Glyph.PENGUIN, SimpleTile(this, 11, 11))
        setTile(Glyph.GRANITE_GIANT, SimpleTile(this, 12, 11))
        setTile(Glyph.BIG_TREE_MAN, SimpleTile(this, 13, 0))
        setTile(Glyph.SCORPIYAK, SimpleTile(this, 13, 2))
    }
