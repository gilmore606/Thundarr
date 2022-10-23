package world

import render.tilesets.Glyph

interface Entity {

    fun glyph(): Glyph
    fun name(): String
    fun description(): String
}
