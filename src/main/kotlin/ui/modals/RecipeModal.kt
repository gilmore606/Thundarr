package ui.modals

import actors.actions.Make
import actors.actions.Wait
import audio.Speaker
import render.Screen
import render.tilesets.Glyph
import things.Thing
import things.recipes.Recipe
import ui.input.Keydef
import ui.input.Mouse
import ui.panels.Console
import util.aOrAn
import util.plural
import util.wrapText

class RecipeModal(
    val workbenchModal: WorkbenchModal,
    val recipe: Recipe
) : Modal(400, 230 + recipe.ingredients().size * 27 + 44 + 90, recipe.name(), position = Position.LEFT) {

    class LineItem(
        val ingredient: Recipe.Ingredient,
        val name: String,
        val glyph: Glyph,
        val hue: Float,
        val things: MutableList<Thing> = mutableListOf(),
        var available: Int = 0
    ) {
        fun filled() = things.size >= ingredient.amount
    }

    private val bench = workbenchModal.bench
    private val makeVerb = bench.craftVerb().capitalize() + "!"
    private val padding = 22
    private val ingpad = 56
    private val spacing = 27
    private val headerPad = 240
    private val buttonX0 = listOf(62, 264)
    private val buttonX1 = listOf(130, 340)

    private val wrappedDesc = wrapText(recipe.description(), width - 64, padding, Screen.font)

    private val lineItems: MutableList<LineItem> = mutableListOf()
    private var productCount = 0
    private val productTag: Thing.Tag
    private val productName: String
    private var makeOK: Boolean = false
    private var selection = 0

    override fun myXmargin() = 120

    init {
        recipe.product().also {
            productTag = it.tag
            productName = it.name()
        }
        zoomWhenOpen = 1.4f

        recipe.ingredients().forEach { ingredient ->
            lineItems.add(LineItem(ingredient, ingredient.description(), ingredient.glyph(), ingredient.hue()))
        }
        updateIngredients()
    }

    override fun advanceTime(delta: Float) {
        updateIngredients()
        super.advanceTime(delta)
    }

    private fun updateIngredients() {
        val pool = mutableListOf<Thing>().apply { addAll(App.player.contents()) }
        makeOK = true
        lineItems.forEach { lineItem ->
            lineItem.available = App.player.contents().count { lineItem.ingredient.matches(it) }
            lineItem.things.clear()
            repeat (lineItem.ingredient.amount) {
                pool.firstOrNull { lineItem.ingredient.matches(it) }?.also { cand ->
                    lineItem.things.add(cand)
                    pool.remove(cand)
                }
            }
            makeOK = makeOK && lineItem.filled()
        }
        productCount = App.player.contents().count { it.tag == productTag }
        if (!makeOK) selection = 0
    }


    override fun onKeyDown(key: Keydef) {
        super.onKeyDown(key)
        when (key) {
            Keydef.MOVE_W -> selectPrevious()
            Keydef.MOVE_E -> selectNext()
            Keydef.INTERACT -> doSelect()
        }
    }

    override fun onMouseMovedTo(screenX: Int, screenY: Int) {
        changeSelection(mouseToOption(screenX, screenY) ?: -1)
    }

    private fun mouseToOption(screenX: Int, screenY: Int): Int? {
        val lx = screenX - x
        val ly = screenY - y
        if (ly in height-65..height-padding) {
            if (lx in buttonX0[0]-5..buttonX1[0]+5) return 0
            if (lx in buttonX0[1]-5..buttonX1[1]+5 && makeOK) return 1
        }
        return null
    }

    override fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        super.onMouseClicked(screenX, screenY, button)
        doSelect()
        return true
    }

    private fun selectPrevious() {
        if (!makeOK) return
        changeSelection(if (selection == 1) 0 else 1)
    }
    private fun selectNext() {
        if (!makeOK) return
        changeSelection(if (selection == 1) 0 else 1)
    }

    private fun changeSelection(newSelection: Int) {
        selection = newSelection
        Speaker.ui(Speaker.SFX.UIMOVE, screenX = x)
    }

    private fun doSelect() {
        Speaker.ui(Speaker.SFX.UISELECT, screenX = x)
        when (selection) {
            0 -> dismiss()
            1 -> tryMake()
        }
    }

    private fun tryMake() {
        val components = mutableListOf<Thing>().apply {
            lineItems.forEach { addAll(it.things) }
        }
        App.player.queue(Make(recipe.makeDuration(), recipe.tag(), components))
    }

    fun onMakeFinish() {
        updateIngredients()
    }

    override fun drawText() {
        super.drawText()
        if (isAnimating()) return
        drawCenterText(recipe.skill().name + " " + recipe.describeDifficulty(), padding, padding + 36, width - (padding*2), Screen.fontColorDull, Screen.smallFont)

        drawWrappedText(wrappedDesc, padding, padding + 70, 24, Screen.font)

        drawString("To ${bench.craftVerb()} ${recipe.name().aOrAn()}, you need:", padding, headerPad - 40, Screen.fontColorDull, Screen.smallFont)

        lineItems.forEachIndexed { i, lineItem ->
            drawString(lineItem.name, ingpad + 30, headerPad + i * spacing,
                if (lineItem.filled()) Screen.fontColorBold else Screen.fontColorDull, Screen.font)
            if (lineItem.available > 0) {
                drawString(lineItem.available.toString(), width - ingpad + 8, headerPad + i * spacing + 1,
                    if (lineItem.filled()) Screen.fontColor else Screen.fontColorDull, Screen.smallFont)
            }
        }

        drawString("You have " + (if (productCount < 1) "no" else productCount.toString()) + " " + (if (productCount == 1) productName else productName.plural()) + ".",
            padding, height - 100, Screen.fontColorDull, Screen.smallFont)

        drawString("Back", padding + 55, height - 50, if (selection == 0) Screen.fontColorBold else Screen.fontColorDull, Screen.font)
        if (makeOK) {
            drawString(makeVerb, width - padding - 105, height - 50, if (selection == 1) Screen.fontColorBold else Screen.fontColorDull, Screen.font)
        }
    }

    override fun drawBackground() {
        super.drawBackground()
        if (isAnimating()) return

        lineItems.forEachIndexed { i, ing ->
            if (ing.filled()) {
                val ix0 = x + width - ingpad - 26
                val iy0 = y + headerPad + i * spacing - 6
                boxBatch.addPixelQuad(ix0, iy0, ix0 + 26, iy0 + 26, boxBatch.getTextureIndex(Glyph.CHECK_MARK))
            }
        }

        if (selection > -1) {
            boxBatch.addPixelQuad(
                x + buttonX0[selection], y + height - 60, x + buttonX1[selection], y + height - 25,
                boxBatch.getTextureIndex(Glyph.BOX_SHADOW)
            )
        }
    }

    override fun drawEntities() {
        if (isAnimating()) return
        val x0 = x + width - padding - 64
        val y0 = y + padding
        val batch = myThingBatch()
        batch?.addPixelQuad(x0, y0, x0 + 64, y0 + 64,
            batch.getTextureIndex(recipe.glyph()))

        lineItems.forEachIndexed { i, ing ->
            val ix0 = x + ingpad
            val iy0 = y + headerPad + i * spacing - 12
            myThingBatch()?.also { batch ->
                batch.addPixelQuad(ix0, iy0, ix0 + 32, iy0 + 32, batch.getTextureIndex(ing.glyph), hue = ing.hue)
            }
        }
    }
}
