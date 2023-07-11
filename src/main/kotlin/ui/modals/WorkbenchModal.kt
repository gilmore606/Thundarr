package ui.modals

import audio.Speaker
import render.Screen
import things.Thing
import things.Workbench
import things.recipes.Recipe
import ui.input.Keydef
import ui.input.Mouse
import util.wrapText
import java.lang.Math.max
import java.lang.Math.min

class WorkbenchModal(
    val bench: Workbench
) : Modal(600, 500, title = bench.workbenchTitle(), position = Position.LEFT) {

    companion object {
        private const val minHeight = 300
    }

    private val padding = 22
    private val spacing = 27
    private val col2x = 300
    private val headerPad = 210
    private val wrappedDesc = wrapText(bench.workbenchDescription() + "\n \nWhat will you ${bench.craftVerb()}?", width - 64, padding, Screen.font)

    private val allRecipes = bench.getRecipes()
    private val goodRecipes = mutableListOf<Recipe>()
    private val badRecipes = mutableListOf<IncompleteRecipe>()

    private var selection = -1
    private var selectionColumn = 0
    private val maxSelection = mutableListOf(-1, -1)
    private val colx0 = listOf(padding, col2x)
    private val colWidth = 260

    class IncompleteRecipe(
        val recipe: Recipe,
        val missing: Int,
        val found: Int
    )

    init {
        zoomWhenOpen = 1.4f

        updateRecipes()
        changeSelection(0, if (goodRecipes.isNotEmpty()) 0 else 1)
    }

    private fun changeSelection(newSelection: Int, newColumn: Int? = null) {
        newColumn?.also { newColumn ->
            if (maxSelection[newColumn] >= 0) {
                selectionColumn = newColumn
            }
        }
        if (newSelection != selection) {
            Speaker.ui(Speaker.SFX.UIMOVE, screenX = x)
        }
        selection = max(-1, min(newSelection, maxSelection[selectionColumn]))
    }

    private fun selectNext() {
        val max = maxSelection[selectionColumn]
        changeSelection(if (selection >= max) if (max < 0) -1 else 0 else selection + 1)
    }

    private fun selectPrevious() {
        changeSelection(if (selection <= 0) maxSelection[selectionColumn] else selection - 1)
    }

    private fun switchColumns() {
        changeSelection(selection, if (selectionColumn == 1) 0 else 1)
    }

    private fun doSelect() {
        if (selection < 0) return
        Speaker.ui(Speaker.SFX.UISELECT, screenX = x)
        val recipe = if (selectionColumn == 0) goodRecipes[selection] else badRecipes[selection].recipe
        Screen.addModal(RecipeModal(this, recipe))
    }

    override fun onMouseMovedTo(screenX: Int, screenY: Int) {
        mouseToOption(screenX, screenY)?.also {
            changeSelection(it.first, it.second)
        } ?: run {
            changeSelection(-1)
        }
    }

    private fun mouseToOption(screenX: Int, screenY: Int): Pair<Int,Int>? {
        val lx = screenX - x
        val ly = screenY - y
        if (ly in headerPad..headerPad + spacing * max(goodRecipes.size, badRecipes.size)) {
            if (lx in padding..width-padding) {
                val s1 = (ly - headerPad) / spacing
                val s2 = if (lx in padding - 5..col2x - 10) 0 else 1
                if (s2 == 0 && s1 < goodRecipes.size || s2 == 1 && s1 < badRecipes.size) return Pair(s1, s2)
            }
        }
        return null
    }

    override fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        doSelect()
        return true
    }

    override fun onKeyDown(key: Keydef) {
        super.onKeyDown(key)
        when (key) {
            Keydef.MOVE_N -> selectPrevious()
            Keydef.MOVE_S -> selectNext()
            Keydef.MOVE_W -> {
                val othercol = if (selectionColumn == 0) badRecipes else goodRecipes
                if (othercol.isNotEmpty()) switchColumns()
                else dismiss()
            }
            Keydef.MOVE_E -> {
                switchColumns()
            }
            Keydef.INTERACT -> doSelect()
            else -> { }
        }
    }

    override fun drawText() {
        super.drawText()
        if (isAnimating()) return
        drawWrappedText(wrappedDesc, padding, padding + 60, 24, Screen.font)

        drawString("Can ${bench.craftVerb()}:", padding, headerPad - 30, Screen.fontColorDull, Screen.smallFont)
        goodRecipes.forEachIndexed { i, recipe ->
            drawRecipeLine(i, recipe, padding + 30, true)
        }

        drawString("Need ${bench.ingredientWord()}s for:", col2x, headerPad - 30, Screen.fontColorDull, Screen.smallFont)
        badRecipes.forEachIndexed { i, recipe ->
            drawRecipeLine(i, recipe.recipe, col2x + 30, false, "(need ${recipe.missing})")
        }
    }

    private fun drawRecipeLine(i: Int, recipe: Recipe, atX: Int, isGood: Boolean, tag: String? = null) {
        drawString(recipe.name(), atX, headerPad + i * spacing, if (isGood) Screen.fontColor else Screen.fontColorDull)
        tag?.also { tag ->
            val x = measure(recipe.name()) + 10
            drawString(tag, atX + x, headerPad + i * spacing + 2, Screen.fontColorDull, Screen.smallFont)
        }
    }

    override fun drawEntities() {
        if (isAnimating()) return
        val x0 = x + width - padding - 64
        val y0 = y + padding
        val batch = myThingBatch()
        batch?.addPixelQuad(x0, y0, x0 + 64, y0 + 64,
            batch.getTextureIndex(bench.workbenchGlyph()),
            hue = bench.hue())

        goodRecipes.forEachIndexed { i, recipe ->
            drawRecipeGlyph(i, recipe, padding)
        }
        badRecipes.forEachIndexed { i, recipe ->
            drawRecipeGlyph(i, recipe.recipe, col2x)
        }
    }

    private fun drawRecipeGlyph(i: Int, recipe: Recipe, atX: Int) {
        val x0 = x + atX
        val y0 = y + headerPad + spacing * i - 12
        myThingBatch()?.also { batch ->
            batch.addPixelQuad(x0, y0, x0 + 32, y0 + 32, batch.getTextureIndex(recipe.glyph()))
        }
    }

    override fun drawBackground() {
        super.drawBackground()
        drawOptionShade()
    }

    private fun drawOptionShade() {
        if (!isAnimating() && selection >= 0) {
            drawSelectionBox(colx0[selectionColumn], headerPad + selection * spacing + 2, colWidth, 20)
        }
    }

    override fun advanceTime(delta: Float) {
        updateRecipes()
        super.advanceTime(delta)
    }

    private fun updateRecipes() {
        goodRecipes.clear()
        badRecipes.clear()
        allRecipes.forEach { recipe ->
            val pool = mutableListOf<Thing>().apply { addAll(App.player.contents()) }
            var missing = 0
            var found = 0
            recipe.ingredients().forEach { ingTag ->
                pool.firstOrNull { it.tag == ingTag }?.also { thing ->
                    pool.remove(thing)
                    found++
                } ?: run {
                    missing++
                }
            }
            if (missing == 0) {
                goodRecipes.add(recipe)
            } else if (found > 0) {
                badRecipes.add(IncompleteRecipe(recipe, missing, found))
            }
        }
        badRecipes.sortBy { it.missing }
        maxSelection[0] = goodRecipes.size - 1
        maxSelection[1] = badRecipes.size - 1
        selection = min(selection, maxSelection[selectionColumn])

        adjustHeight()
    }

    private fun adjustHeight() {
        val maxItems = max(maxSelection[0], maxSelection[1])
        height = max(minHeight, headerPad + spacing * maxItems + padding + 30)
        onResize(Screen.width, Screen.height)
    }
}
