package ui.modals

import com.badlogic.gdx.Input
import render.Screen
import render.batches.QuadBatch
import render.tilesets.Glyph
import ui.input.Keyboard
import ui.input.Keydef
import ui.input.Mouse
import util.*
import world.gen.Metamap
import world.gen.biomes.Ocean
import world.gen.features.*
import world.gen.habitats.Blank
import java.lang.Integer.max
import java.lang.Integer.min

class MapModal : Modal(1200, 900, "- yOUr tRAvELs -") {


    val playerX = Metamap.chunkXtoX(App.player.xy.x)
    val playerY = Metamap.chunkYtoY(App.player.xy.y)

    var cellSize = 20
    var mapx = 0
    var mapy = 0

    var revealAll = false
    var showHabitats = false

    override fun newThingBatch() = null
    override fun newActorBatch() = null

    override fun onAdd() {
        centerView()
    }

    override fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        dismiss()
        return true
    }

    override fun onKeyDown(key: Keydef) {
        when (key) {
            Keydef.CANCEL -> dismiss()
            Keydef.ZOOM_OUT -> {
                cellSize = max(6, cellSize - 2)
                centerView()

            }
            Keydef.ZOOM_IN -> {
                cellSize = min(20, cellSize + 2)
                centerView()

            }
            Keydef.MOVE_N -> mapy--
            Keydef.MOVE_S -> mapy++
            Keydef.MOVE_W -> mapx--
            Keydef.MOVE_E -> mapx++
            Keydef.MOVE_NW -> { mapy-- ; mapx-- }
            Keydef.MOVE_NE -> { mapy-- ; mapx++ }
            Keydef.MOVE_SW -> { mapy++ ; mapx-- }
            Keydef.MOVE_SE -> { mapy++ ; mapx++ }

            Keydef.DEBUG_F9 -> { revealAll = !revealAll }

            else -> { }
        }
    }

    fun centerView() {
        mapx = playerX - (1200 / cellSize) / 2
        mapy = playerY - (900 / cellSize) / 2
    }

    fun renderMap(batch: QuadBatch) {
        if (isAnimating()) return
        val x0 = x + 30
        val y0 = y + 70
        for (x in 0 until (1120 / cellSize)) {
            for (y in 0 until (800 / cellSize)) {
                val meta = Metamap.metaAt(x+mapx, y+mapy)
                if (meta.mapped || revealAll) {
                    val ox = x * cellSize
                    val oy = y * cellSize
                    val px0 = x0 + ox
                    val py0 = y0 + oy
                    val px1 = px0 + cellSize
                    val py1 = py0 + cellSize
                    meta.mapIcons.forEach { mapIcon ->
                        batch.addPixelQuad(px0, py0, px1, py1, Screen.mapBatch.getTextureIndex(mapIcon))
                    }
                    if (x + mapx == playerX && y + mapy == playerY) {
                        batch.addPixelQuad(px0, py0, px1, py1, Screen.mapBatch.getTextureIndex(Glyph.MAP_PLAYER))
                    }
                    if (showHabitats && meta.habitat != Blank) {
                        batch.addPixelQuad(px0, py0, px1, py1, Screen.mapBatch.getTextureIndex(meta.habitat.mapGlyph))
                    }
                }
            }
        }
    }
}
