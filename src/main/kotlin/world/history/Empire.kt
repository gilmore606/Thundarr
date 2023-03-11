package world.history

import App
import kotlinx.serialization.Serializable
import render.tilesets.Glyph

@Serializable
class Empire(
    val id: Int,
    var mapColor: Glyph = listOf(Glyph.MAP_COLOR_0, Glyph.MAP_COLOR_1, Glyph.MAP_COLOR_2, Glyph.MAP_COLOR_3,
        Glyph.MAP_COLOR_4, Glyph.MAP_COLOR_5, Glyph.MAP_COLOR_6, Glyph.MAP_COLOR_7, Glyph.MAP_COLOR_8,
        Glyph.MAP_COLOR_9, Glyph.MAP_COLOR_10, Glyph.MAP_COLOR_11, Glyph.MAP_COLOR_12, Glyph.MAP_COLOR_13,
        Glyph.MAP_COLOR_14, Glyph.MAP_COLOR_15).random()
) {

    var leader: Int = 0

    var active: Boolean = true
    var foundingYear: Int = App.history.year
    var ruinYear: Int = 0

    fun shortName() = App.history.figure(leader)?.name ?: "MYSTERY"

}
