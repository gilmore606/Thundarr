package render.tilesets

import com.badlogic.gdx.graphics.Texture
import render.Screen
import render.tileholders.AnimatedTile
import render.tileholders.SimpleTile
import render.tileholders.VariantsTile

fun ThingTileSet() =
    TileSet("tiles_thing.png", 6, 6,
        Screen.textureFilter).apply {

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
        setTile(Glyph.BOW, SimpleTile(this, 4, 3))
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
        setTile(Glyph.CORPSE, SimpleTile(this, 3, 5))
    }
