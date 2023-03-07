package ui.modals

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import render.Screen
import render.batches.QuadBatch
import render.tilesets.Glyph
import render.tilesets.MapTileSet
import ui.input.Keydef
import ui.input.Mouse
import util.Dice
import util.Madlib
import world.gen.Metamap

class HistogenModal : Modal(1300, 800, "- nUMeRiA -") {

    private val wizardCount = 7

    data class Wizard(
        val name: String,
        val color: Glyph,
        val cunning: Float = Dice.float(0f,1f),
        val cruelty: Float = Dice.float(0f, 1f),
        val madness: Float = Dice.float(0f, 1f),
    )

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
    var year = 1994
    val wizards: ArrayList<Wizard> = ArrayList()

    var selection = 0

    init {
        this.width = Screen.width - 100
        this.height = Screen.height - 100
        this.x = 50
        this.y = 50
    }

    override fun getTitleForDisplay() = "- nUMeRiA : AD $year -"

    private fun renderMap() {
        val batch = mapBatch
        if (isAnimating()) return
        val x0 = x + paddingX
        val y0 = y + paddingY
        for (x in 0 until ((width - paddingX*2 - rightPad) / cellSize)) {
            for (y in 0 until ((height - paddingY - paddingX) / cellSize)) {
                val meta = Metamap.metaAt(x+mapx, y+mapy)
                val ox = x * cellSize
                val oy = y * cellSize
                val px0 = x0 + ox
                val py0 = y0 + oy
                val px1 = px0 + cellSize
                val py1 = py0 + cellSize
                meta.mapIcons.forEach { mapIcon ->
                    batch.addPixelQuad(px0, py0, px1, py1, batch.getTextureIndex(mapIcon))
                }
            }
        }

        if (started) {
            wizards.forEachIndexed { i, wizard ->
                val x0 = x + dataX
                val y0 = y + paddingY + 60 + i*wizSpacing
                mapBatch.addPixelQuad(x0, y0, x0 + 16, y0 + 16, mapBatch.getTextureIndex(Glyph.MAP_GLACIER))
                mapBatch.addPixelQuad(x0 + 2, y0 + 2, x0 + 14, y0 + 14, mapBatch.getTextureIndex(wizard.color))
            }
        }
    }

    override fun drawModalText() {
        drawString(headerText, dataX, paddingY, color = Screen.fontColor, font = Screen.smallFont)

        if (!started) {

            drawString("Re-generate the world", dataX, paddingY + 50, color = if (selection == 0) Screen.fontColorBold else Screen.fontColorDull)
            drawString("Continue", dataX, paddingY + 85, color = if (selection == 1) Screen.fontColorBold else Screen.fontColorDull)

        } else {

            wizards.forEachIndexed { i, wizard ->
                drawString(wizard.name, dataX + 25, paddingY + 60 + i*wizSpacing)
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
                (year - 1994), 1001, allGreen = true)

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

        val wizardColors = mutableListOf(Glyph.MAP_COLOR_0, Glyph.MAP_COLOR_1, Glyph.MAP_COLOR_2, Glyph.MAP_COLOR_3,
            Glyph.MAP_COLOR_4, Glyph.MAP_COLOR_5, Glyph.MAP_COLOR_6, Glyph.MAP_COLOR_7, Glyph.MAP_COLOR_8,
            Glyph.MAP_COLOR_9, Glyph.MAP_COLOR_10, Glyph.MAP_COLOR_11, Glyph.MAP_COLOR_12, Glyph.MAP_COLOR_13,
            Glyph.MAP_COLOR_14, Glyph.MAP_COLOR_15)
        wizards.clear()
        repeat (wizardCount) {
            val color = wizardColors.random()
            wizardColors.remove(color)
            wizards.add(Wizard(
                name = Madlib.wizardFullName(Madlib.wizardName()),
                color = color,
            ))
        }
    }

}
