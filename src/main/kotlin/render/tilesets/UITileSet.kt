package render.tilesets

import RESOURCE_FILE_DIR
import com.badlogic.gdx.graphics.Texture
import render.tileholders.SimpleTile

fun UITileSet() =
    TileSet("tiles_ui.png", 4, 3,
            Texture.TextureFilter.Nearest).apply {
        setTile(Glyph.CURSOR, SimpleTile(this, 0, 0))
        setTile(Glyph.BOX_BG, SimpleTile(this, 1, 0))
        setTile(Glyph.BOX_SHADOW, SimpleTile(this, 0, 1))
        setTile(Glyph.BOX_BORDER, SimpleTile(this, 1, 1))
        setTile(Glyph.LOGO_MOON, SimpleTile(this, 2, 0))
        setTile(Glyph.LOGO_OOKLA, SimpleTile(this, 2, 1))
        setTile(Glyph.PANEL_SHADOW, SimpleTile(this, 0, 2))
        setTile(Glyph.BUTTON_INVENTORY, SimpleTile(this, 1, 2))
        setTile(Glyph.BUTTON_MAP, SimpleTile(this, 2, 2))
        setTile(Glyph.BUTTON_SYSTEM, SimpleTile(this, 3, 2))
        setTile(Glyph.BUTTON_JOURNAL, SimpleTile(this, 3, 1))
    }
