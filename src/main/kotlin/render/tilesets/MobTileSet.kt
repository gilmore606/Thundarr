package render.tilesets

import RESOURCE_FILE_DIR
import com.badlogic.gdx.graphics.Texture
import render.tileholders.SimpleTile

fun MobTileSet() =
    TileSet("tiles_mob.png", 4, 2,
            Texture.TextureFilter.MipMap).apply {
        setTile(Glyph.PLAYER, SimpleTile(this, 0, 1))
        setTile(Glyph.SKELETON, SimpleTile(this, 1, 0))
        setTile(Glyph.SKULL, SimpleTile(this, 1, 1))
    }
