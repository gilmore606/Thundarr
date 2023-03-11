package ui.modals

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import render.Screen
import render.batches.QuadBatch
import render.tilesets.Glyph
import render.tilesets.MapTileSet
import ui.input.Keydef
import ui.input.Mouse
import world.gen.Metamap
import world.level.CHUNK_SIZE

class HistogenModal : Modal(1300, 800, "- nUMeRiA -") {

    override fun newThingBatch() = null
    override fun newActorBatch() = null
    val mapBatch = QuadBatch(MapTileSet())

    var cellSize = 5
    var mapx = 7
    var mapy = 16

    private val paddingX = 30
    private val paddingY = 70
    private val rightPad = 200
    private val wizSpacing = 30

    private val dataX = width - rightPad - paddingX

    private var headerText: String = "A new world rises from the ashes..."

    var started = false

    var selection = 0

    init {
        this.width = Screen.width - 100
        this.height = Screen.height - 100
        this.x = 50
        this.y = 50
    }

    override fun getTitleForDisplay() = "- nUMeRiA : ${App.history.year} -"

    private fun renderMap() {
        if (isAnimating()) return
        for (x in 0 until ((width - paddingX*2 - rightPad) / cellSize)) {
            for (y in 0 until ((height - paddingY - paddingX) / cellSize)) {
                val meta = Metamap.metaAt(x+mapx, y+mapy)
                meta.mapIcons.forEach { mapIcon ->
                    renderCell(x, y, mapIcon)
                }
            }
        }
        if (App.history.renderLocked) return

        App.history.strongholds.forEach { strongHold ->
            strongHold.empire?.also { empireId ->
                val empire = App.history.empire(empireId)
                renderCell(chunkXtoX(strongHold.worldX), chunkYtoY(strongHold.worldY), empire!!.mapColor, 5f)
            }
        }

        // Draw right-side legend
        if (started) {
            App.history.empires.values.forEachIndexed { i, empire ->
                val x0 = x + dataX
                val y0 = y + paddingY + 60 + i*wizSpacing
                mapBatch.addPixelQuad(x0, y0, x0 + 16, y0 + 16, mapBatch.getTextureIndex(Glyph.MAP_GLACIER))
                mapBatch.addPixelQuad(x0 + 2, y0 + 2, x0 + 14, y0 + 14, mapBatch.getTextureIndex(empire.mapColor))
            }
        }
    }

    fun chunkXtoX(x: Int) = (x + (Metamap.chunkRadius * CHUNK_SIZE)) / CHUNK_SIZE
    fun chunkYtoY(y: Int) = (y + (Metamap.chunkRadius * CHUNK_SIZE)) / CHUNK_SIZE

    private fun renderCell(x: Int, y: Int, glyph: Glyph, zoom: Float = 1f) {
        val ox = x * cellSize
        val oy = y * cellSize
        val size = cellSize * zoom
        val px0 = this.x + paddingX + ox - (size - cellSize) / 2
        val py0 = this.y + paddingY + oy - (size - cellSize) / 2
        val px1 = px0 + cellSize + (size - cellSize) / 2
        val py1 = py0 + cellSize + (size - cellSize) / 2
        mapBatch.addPixelQuad(px0.toInt(), py0.toInt(), px1.toInt(), py1.toInt(), mapBatch.getTextureIndex(glyph))
    }

    override fun drawModalText() {
        drawString(headerText, dataX, paddingY, color = Screen.fontColor, font = Screen.smallFont)

        if (!started) {

            drawString("Re-generate the world", dataX, paddingY + 50, color = if (selection == 0) Screen.fontColorBold else Screen.fontColorDull)
            drawString("Continue", dataX, paddingY + 85, color = if (selection == 1) Screen.fontColorBold else Screen.fontColorDull)

        } else {

            App.history.empires.values.forEachIndexed { i, empire ->
                drawString(empire.shortName(), dataX + 25, paddingY + 60 + i*wizSpacing)
            }

        }
    }

    override fun drawBackground() {
        super.drawBackground()
        if (!started) {

            if (selection > -1) {
                drawSelectionBox(dataX, paddingY + 50 + (selection * 35), 200, 30)
            }

        } else {

            myBoxBatch().addHealthBar(dataX, paddingY + 20, dataX + 180, paddingY + 20,
                (App.history.year - 1994), 1001, allGreen = true)

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

    override fun onMouseMovedTo(screenX: Int, screenY: Int) {
        val lx = screenX - x
        val ly = screenY - y
        if (!started) {

            selection = -1
            if (lx >= dataX) {
                if (ly in paddingY+40..paddingY+75) selection = 0
                if (ly in paddingY+76..paddingY+100) selection = 1
            }

        } else {

        }
    }

    override fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        if (!started) {

            preSelect()

        } else {

            dismiss()
            Metamap.finishBuildWorld()

        }
        return true
    }

    override fun onKeyDown(key: Keydef) {
        if (!started) {

            when (key) {
                Keydef.MOVE_N, Keydef.MOVE_W, Keydef.MOVE_E, Keydef.MOVE_S -> {
                    if (selection == 1) selection = 0 else selection = 1
                }
                Keydef.INTERACT -> {
                    preSelect()
                }
            }

        } else {

        }
    }

    private fun preSelect() {
        when (selection) {
            0 -> regenerateWorld()
            1 -> startHistory()
        }
    }

    private fun regenerateWorld() {
        dismiss()
        Metamap.rejectWorld()
    }

    private fun startHistory() {
        started = true
        Metamap.startHistory()
    }

}
