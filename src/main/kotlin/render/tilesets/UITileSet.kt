package render.tilesets

import render.tileholders.SimpleTile

fun UITileSet() =
    TileSet(SpriteSheets.Sheet.UISprites).apply {
        setTile(Glyph.CURSOR, SimpleTile(this, 0, 0))
        setTile(Glyph.BOX_BG, SimpleTile(this, 1, 0))
        setTile(Glyph.BOX_SHADOW, SimpleTile(this, 0, 1))
        setTile(Glyph.BOX_BORDER, SimpleTile(this, 1, 1))
        setTile(Glyph.LOGO_MOON, SimpleTile(this, 2, 0))
        setTile(Glyph.LOGO_OOKLA, SimpleTile(this, 2, 1))
        setTile(Glyph.PANEL_SHADOW, SimpleTile(this, 0, 2))
        setTile(Glyph.BUTTON_INVENTORY, SimpleTile(this, 1, 2))
        setTile(Glyph.BUTTON_GEAR, SimpleTile(this, 3, 0))
        setTile(Glyph.BUTTON_MAP, SimpleTile(this, 2, 2))
        setTile(Glyph.BUTTON_SYSTEM, SimpleTile(this, 3, 2))
        setTile(Glyph.BUTTON_JOURNAL, SimpleTile(this, 3, 1))
        setTile(Glyph.BUTTON_SKILLS, SimpleTile(this, 4, 3))
        setTile(Glyph.BUTTON_PAUSE, SimpleTile(this, 4, 0))
        setTile(Glyph.BUTTON_PLAY, SimpleTile(this, 4, 1))
        setTile(Glyph.BUTTON_FFWD, SimpleTile(this, 4, 2))
        setTile(Glyph.BUTTON_BLANK, SimpleTile(this, 4, 4))
        setTile(Glyph.COLOR_BARS, SimpleTile(this, 0, 3))
        setTile(Glyph.HEALTH_ICON, SimpleTile(this, 1, 3))
        setTile(Glyph.SLEEP_ICON, SimpleTile(this, 3, 4))
        setTile(Glyph.ANGRY_THUNDARR, SimpleTile(this, 2, 3))
        setTile(Glyph.SPEECH_BUBBLE, SimpleTile(this, 3, 3))
        setTile(Glyph.POW_ICON, SimpleTile(this, 0, 4))
        setTile(Glyph.HOSTILE_ICON, SimpleTile(this, 1, 4))
        setTile(Glyph.WINDOW_SHADE, SimpleTile(this, 2, 4))
        setTile(Glyph.QUESTION_ICON, SimpleTile(this, 5, 4))
        setTile(Glyph.INVENTORY_ALL, SimpleTile(this, 5, 0))
        setTile(Glyph.INVENTORY_GEAR, SimpleTile(this, 5, 1))
        setTile(Glyph.INVENTORY_CONSUMABLES, SimpleTile(this, 5, 2))
        setTile(Glyph.INVENTORY_TOOLS, SimpleTile(this, 5, 3))
        setTile(Glyph.INVENTORY_MISC, SimpleTile(this, 5, 5))
    }
