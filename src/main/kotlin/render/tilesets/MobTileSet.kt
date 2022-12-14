package render.tilesets

import com.badlogic.gdx.graphics.Texture
import render.Screen
import render.tileholders.AnimatedTile
import render.tileholders.SimpleTile

fun ActorTileSet() =
    TileSet(SpriteSheets.Sheet.ActorSprites).apply {

        setTile(Glyph.MOB_SHADOW, SimpleTile(this, 3, 1))

        setTile(Glyph.PLAYER, SimpleTile(this, 0, 1))
        setTile(Glyph.MOK, SimpleTile(this, 2, 1))
        setTile(Glyph.TRIBAL, SimpleTile(this, 3, 0))
        setTile(Glyph.CITIZEN, SimpleTile(this, 1, 1))
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
        setTile(Glyph.HERDER, SimpleTile(this, 1, 0))
        setTile(Glyph.WOLFMAN, SimpleTile(this, 0, 3))
        setTile(Glyph.SNAKEMAN, SimpleTile(this, 1, 3))
        setTile(Glyph.KARROK, SimpleTile(this, 2, 3))
        setTile(Glyph.THRALL, SimpleTile(this, 3, 3))
        setTile(Glyph.RATMAN, SimpleTile(this, 0, 4))
        setTile(Glyph.RATTHING, SimpleTile(this, 1, 4))
        setTile(Glyph.RATLORD, SimpleTile(this, 3, 4))
        setTile(Glyph.FLOATING_EYE, SimpleTile(this, 2, 4))
        setTile(Glyph.WIZARD_SCYTHE, SimpleTile(this, 4, 0))
        setTile(Glyph.WIZARD_SHIELD, SimpleTile(this, 4, 2))
        setTile(Glyph.THRALL_ARCHER, SimpleTile(this, 4, 1))
        setTile(Glyph.FURWORM, SimpleTile(this, 5, 3))
    }
