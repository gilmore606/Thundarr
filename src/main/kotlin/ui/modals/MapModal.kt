package ui.modals

import com.badlogic.gdx.Input
import render.Screen
import render.batches.QuadBatch
import render.tilesets.Glyph
import ui.input.Keyboard
import ui.input.Mouse
import util.*
import world.gen.Metamap
import world.gen.biomes.Ocean
import world.gen.habitats.Blank
import java.lang.Integer.max
import java.lang.Integer.min

class MapModal : Modal(1200, 900, "- yOUr tRAvELs -") {


    val playerX = Metamap.chunkXtoX(App.player.xy.x)
    val playerY = Metamap.chunkYtoY(App.player.xy.y)

    var cellSize = 20
    var mapx = 0
    var mapy = 0

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

    override fun onKeyDown(keycode: Int) {
        if (keycode == Input.Keys.ESCAPE) dismiss()
        else if (keycode == Input.Keys.MINUS) {
            cellSize = max(6, cellSize - 2)
            centerView()
        } else if (keycode == Input.Keys.EQUALS) {
            cellSize = min(20, cellSize + 2)
            centerView()
        } else if (keycode == Input.Keys.F1) {
            showHabitats = !showHabitats
        }
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
                val ox = x * cellSize
                val oy = y * cellSize
                val px0 = x0 + ox
                val py0 = y0 + oy
                val px1 = px0 + cellSize
                val py1 = py0 + cellSize
                batch.addPixelQuad(px0, py0, px1, py1, Screen.mapBatch.getTextureIndex(meta.biome.mapGlyph))
                var isNorth = false
                var isSouth = false
                var isEast = false
                var isWest = false
                if (meta.biome != Ocean && meta.riverExits.isNotEmpty()) {
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
                if (meta.roadExits.isNotEmpty()) {
                    meta.roadExits.forEach { exit ->
                        when (exit.edge) {
                            NORTH -> isNorth = true
                            SOUTH -> isSouth = true
                            WEST -> isWest = true
                            EAST -> isEast = true
                        }
                    }
                    val road = when {
                        isNorth && isSouth && isWest && isEast -> Glyph.MAP_ROAD_NSEW
                        isNorth && isSouth && isWest -> Glyph.MAP_ROAD_NSW
                        isNorth && isSouth && isEast -> Glyph.MAP_ROAD_NSE
                        isWest && isEast && isSouth -> Glyph.MAP_ROAD_WES
                        isWest && isEast && isNorth -> Glyph.MAP_ROAD_NWE
                        isNorth && isSouth -> Glyph.MAP_ROAD_NS
                        isWest && isEast -> Glyph.MAP_ROAD_WE
                        isNorth && isWest -> Glyph.MAP_ROAD_WN
                        isSouth && isWest -> Glyph.MAP_ROAD_WS
                        isSouth && isEast -> Glyph.MAP_ROAD_SE
                        isNorth && isEast -> Glyph.MAP_ROAD_NE
                        isNorth || isSouth -> Glyph.MAP_ROAD_NS
                        isWest || isEast -> Glyph.MAP_ROAD_WE
                        else -> Glyph.MAP_ROAD_SE
                    }
                    batch.addPixelQuad(px0, py0, px1, py1, Screen.mapBatch.getTextureIndex(road))
                }
                if (meta.hasCity) {
                    batch.addPixelQuad(px0, py0, px1, py1, Screen.mapBatch.getTextureIndex(Glyph.MAP_MARKER))
                }
                if (x+mapx == playerX && y+mapy == playerY) {
                    batch.addPixelQuad(px0, py0, px1, py1, Screen.mapBatch.getTextureIndex(Glyph.MAP_PLAYER))
                }
                if (showHabitats && meta.habitat != Blank) {
                    batch.addPixelQuad(px0, py0, px1, py1, Screen.mapBatch.getTextureIndex(meta.habitat.mapGlyph))
                }
            }
        }
    }
}
