package ui.modals

import com.badlogic.gdx.Input
import render.Screen
import render.batches.QuadBatch
import render.tilesets.Glyph
import ui.input.Keyboard
import ui.input.Mouse
import util.*
import world.cartos.Metamap

class MapModal : Modal(860, 600, "- yOUr tRAvELs -") {

    var mapx = 0
    var mapy = 0
    var cellSize = 16

    override fun newThingBatch() = null
    override fun newActorBatch() = null

    override fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        dismiss()
        return true
    }

    override fun onKeyDown(keycode: Int) {
        if (keycode == Input.Keys.ESCAPE) dismiss()
        else when (Keyboard.moveKeys[keycode]) {
            NORTH -> mapy--
            SOUTH -> mapy++
            WEST -> mapx--
            EAST -> mapx++
            NORTHWEST -> { mapy-- ; mapx-- }
            NORTHEAST -> { mapy-- ; mapx++ }
            SOUTHWEST -> { mapy++ ; mapx-- }
            SOUTHEAST -> { mapy++ ; mapx++ }
        }
    }

    fun renderMap(batch: QuadBatch) {
        if (isAnimating()) return
        val x0 = x + 30
        val y0 = y + 70
        for (x in 0 until 50) {
            for (y in 0 until 32) {
                val meta = Metamap.metaAt(x+mapx, y+mapy)
                val ox = x * cellSize
                val oy = y * cellSize
                val px0 = x0 + ox
                val py0 = y0 + oy
                val px1 = px0 + cellSize
                val py1 = py0 + cellSize / 4
                batch.addPixelQuad(px0, py0, px1, py1, Screen.mapBatch.getTextureIndex(meta.biome.mapGlyph))
                if (meta.riverExits.isNotEmpty()) {
                    var isNorth = false
                    var isSouth = false
                    var isEast = false
                    var isWest = false
                    meta.riverExits.forEach { exit ->
                        when (exit.edge) {
                            NORTH -> isNorth = true
                            SOUTH -> isSouth = true
                            WEST -> isWest = true
                            EAST -> isEast = true
                        }
                    }
                    val river = when {
                        isNorth && isSouth && isWest -> Glyph.MAP_RIVER_NSW
                        isNorth && isSouth && isEast -> Glyph.MAP_RIVER_NSE
                        isWest && isEast && isSouth -> Glyph.MAP_RIVER_WES
                        isWest && isEast && isNorth -> Glyph.MAP_RIVER_NWE
                        isNorth && isSouth -> Glyph.MAP_RIVER_NS
                        isWest && isEast -> Glyph.MAP_RIVER_WE
                        isNorth && isWest -> Glyph.MAP_RIVER_WN
                        isSouth && isWest -> Glyph.MAP_RIVER_WS
                        isSouth && isEast -> Glyph.MAP_RIVER_SE
                        isNorth && isEast -> Glyph.MAP_RIVER_NE
                        isNorth || isSouth -> Glyph.MAP_RIVER_NS
                        isWest || isEast -> Glyph.MAP_RIVER_WE
                        else -> Glyph.MAP_RIVER_SE
                    }
                    batch.addPixelQuad(px0, py0, px1, py1, Screen.mapBatch.getTextureIndex(river))
                }
            }
        }
    }
}
