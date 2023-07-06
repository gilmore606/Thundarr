package render.tilesets

import render.tileholders.AnimatedTile
import render.tileholders.SimpleTile

fun ActorTileSet() =
    TileSet(SpriteSheets.Sheet.ActorSprites).apply {

        setTile(Glyph.MOB_SHADOW, SimpleTile(this, 3, 1))

        setTile(Glyph.PLAYER, SimpleTile(this, 0, 1))
        setTile(Glyph.MOK, SimpleTile(this, 2, 1))
        setTile(Glyph.HORSE, AnimatedTile(this).apply {
            frameMs = 300
            add(0, 2)
            add(1, 2)
        })
        setTile(Glyph.CATTLE, AnimatedTile(this).apply {
            frameMs = 450
            add(2, 2)
            add(3, 2)
        })
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
    }
