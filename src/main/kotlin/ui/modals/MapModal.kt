package ui.modals

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import render.Screen
import render.batches.QuadBatch
import render.tilesets.Glyph
import render.tilesets.MapTileSet
import ui.input.Keydef
import ui.input.Mouse
import util.forXY
import world.gen.Metamap
import world.gen.habitats.Blank
import java.lang.Integer.max
import java.lang.Integer.min

class MapModal : Modal(Screen.width - RIGHT_PANEL_WIDTH - 150, Screen.height - 150, "- yOUr tRAvELs -") {

    private val paddingX = 30
    private val paddingY = 70

    private val playerX = Metamap.chunkXtoX(App.player.xy.x)
    private val playerY = Metamap.chunkYtoY(App.player.xy.y)

    var cellSize = 20
    var mapx = 0
    var mapy = 0

    var lastMouseX = -1
    var lastMouseY = -1

    var revealAll = false
    var showHabitats = false
    var showThreat = false

    var poiFloater: MapFloater? = null

    override fun newThingBatch() = null
    override fun newActorBatch() = null
    private val mapBatch = QuadBatch(Screen.mapTileSet)

    override fun onAdd() {
        centerView()
    }

    override fun onMouseMovedTo(screenX: Int, screenY: Int) {
        val lx = screenX - x - paddingX
        val ly = screenY - y - paddingY
        val tx = (lx / cellSize) + mapx
        val ty = (ly / cellSize) + mapy
        if (tx != lastMouseX || ty != lastMouseY) {
            lastMouseX = tx
            lastMouseY = ty
            val meta = Metamap.metaAt(tx,ty)
            var hasFloater = false
            if (meta.mapped || revealAll) {
                var alreadyUp = false
                meta.mapPOITitle()?.also { title ->
                    poiFloater?.also {
                        if (it.title !== title) {
                            it.remoteDismiss()
                            poiFloater = null
                        } else {
                            alreadyUp = true
                            hasFloater = true
                        }
                    }
                    if (!alreadyUp) {
                        val floater =
                            MapFloater(this, screenX + 8, screenY - 4,
                                title, meta.mapPOIDescription() ?: "", meta.mapPOIDiscoveryTime())
                        Screen.addModal(floater)
                        poiFloater = floater
                        hasFloater = true
                    }
                }
            }
            if (!hasFloater) {
                poiFloater?.remoteDismiss()
                poiFloater = null
            }
        }
        super.onMouseMovedTo(screenX, screenY)
    }

    override fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        if (super.onMouseClicked(screenX, screenY, button)) return true
        dismiss()
        return true
    }

    override fun onKeyDown(key: Keydef) {
        when (key) {
            Keydef.CANCEL, Keydef.OPEN_MAP -> dismiss()
            Keydef.OPEN_SKILLS -> replaceWith(SkillsModal(App.player))
            Keydef.OPEN_INV -> replaceWith(ThingsModal(App.player))
            Keydef.OPEN_GEAR -> replaceWith(GearModal(App.player))
            Keydef.OPEN_JOURNAL -> replaceWith(JournalModal())
            Keydef.ZOOM_OUT -> {
                cellSize = max(6, cellSize - 2)
                centerView()

            }
            Keydef.ZOOM_IN -> {
                cellSize = min(30, cellSize + 2)
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
            Keydef.DEBUG_F10 -> { showHabitats = !showHabitats }
            Keydef.DEBUG_F11 -> { showThreat = !showThreat }

            else -> { }
        }
    }

    fun centerView() {
        mapx = playerX - (1200 / cellSize) / 2
        mapy = playerY - (900 / cellSize) / 2
    }

    private fun renderMap() {
        val batch = mapBatch
        if (isAnimating()) return
        val x0 = x + paddingX
        val y0 = y + paddingY
        val mapW = width - paddingX * 2
        val mapH = height - paddingY * 2
        forXY(0,0, (mapW/cellSize)-1, (mapH/cellSize)-1) { x,y ->
            val meta = Metamap.metaAt(x+mapx, y+mapy)
            if (meta.mapped || revealAll) {
                val ox = x * cellSize
                val oy = y * cellSize
                val px0 = x0 + ox
                val py0 = y0 + oy
                val px1 = px0 + cellSize
                val py1 = py0 + cellSize
                meta.mapIcons.forEach { mapIcon ->
                    batch.addPixelQuad(px0, py0, px1, py1, batch.getTextureIndex(mapIcon))
                }
                if (x + mapx == playerX && y + mapy == playerY) {
                    batch.addPixelQuad(px0, py0, px1, py1, batch.getTextureIndex(Glyph.MAP_PLAYER))
                }
                if (showHabitats && meta.habitat != Blank) {
                    batch.addPixelQuad(px0, py0, px1, py1, batch.getTextureIndex(meta.habitat.mapGlyph))
                }
                if (showThreat) {
                    batch.addPixelQuad(px0, py0, px1, py1, batch.getTextureIndex(Glyph.MAP_COLOR_0), alpha = (meta.threatLevel * 0.06f))
                }
            }
        }
    }

    override fun drawText() {
        super.drawText()
        if (!showThreat) return
        val x0 = x + paddingX
        val y0 = y + paddingY
        forXY(0,0, (1120/cellSize)-1, (800/cellSize)-1) { x,y ->
            val meta = Metamap.metaAt(x + mapx, y + mapy)
            val ox = x * cellSize
            val oy = y * cellSize
            val px0 = x0 + ox + cellSize / 3
            val py0 = y0 + oy + cellSize / 3
            drawStringAbsolute(meta.threatLevel.toString(), px0, py0, Screen.fontColor, Screen.smallFont)
        }
    }

    override fun drawEverything() {
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glEnable(GL20.GL_BLEND)

        clearBoxBatch()
        renderBackground()
        drawBoxBatch()

        mapBatch.clear()
        renderMap()
        mapBatch.draw()

        beginTextBatch()
        renderText()
        endTextBatch()
    }

    override fun dispose() {
        super.dispose()
        mapBatch.dispose()
    }
}
