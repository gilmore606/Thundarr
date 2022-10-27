package render.tilesets

import com.badlogic.gdx.graphics.Texture
import render.Screen
import render.tileholders.AnimatedTile
import render.tileholders.SimpleTile

fun ActorTileSet() =
    TileSet("tiles_mob.png", 4, 4,
        Screen.textureFilter).apply {

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
    }
