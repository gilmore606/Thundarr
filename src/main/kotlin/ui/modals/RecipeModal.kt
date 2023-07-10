package ui.modals

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

    class Ingredient(
        val name: String,
        val tag: Thing.Tag,
        var amount: Int,
        val glyph: Glyph,
        val hue: Float,
        val things: MutableList<Thing> = mutableListOf(),
        var total: Int = 0
    ) {
        fun filled() = things.size >= amount
    }

    private val bench = workbenchModal.bench
    private val makeVerb = bench.craftVerb().capitalize() + "!"
    private val padding = 22
    private val ingpad = 56
    private val spacing = 27
    private val headerPad = 240
    private val buttonX0 = listOf(62, 264)
    private val buttonX1 = listOf(130, 330)

    private val wrappedDesc = wrapText(recipe.description(), width - 64, padding, Screen.font)

    private val ingredients: MutableList<Ingredient> = mutableListOf()
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

        recipe.ingredients().forEach { ing ->
            ingredients.firstOrNull { it.tag == ing }?.also {
                it.amount++
            } ?: run {
                val goat = ing.spawn()
                ingredients.add(Ingredient(
                    goat.name(), ing, 1, goat.glyph(), goat.hue()
                ))
            }
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
        ingredients.forEach { ing ->
            ing.total = App.player.contents().count { it.tag == ing.tag }
            ing.things.clear()
            repeat (ing.amount) {
                pool.firstOrNull { it.tag == ing.tag }?.also { cand ->
                    ing.things.add(cand)
                    pool.remove(cand)
                }
            }
            makeOK = makeOK && ing.things.size >= ing.amount
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
        // TODO: remove this and actually queue a Make action to finish
        App.player.queue(Wait(5f))
        doMake()
    }

    private fun doMake() {
        Console.say(recipe.makeSuccessMsg())

        ingredients.forEach {
            it.things.forEach {
                it.moveTo(null)
            }
        }

        recipe.product().moveTo(App.player)
        updateIngredients()
    }

    override fun drawText() {
        super.drawText()
        if (isAnimating()) return
        drawCenterText(recipe.skill().name + " " + recipe.describeDifficulty(), padding, padding + 36, width - (padding*2), Screen.fontColorDull, Screen.smallFont)

        drawWrappedText(wrappedDesc, padding, padding + 70, 24, Screen.font)

        drawString("To ${bench.craftVerb()} ${recipe.name().aOrAn()}, you need:", padding, headerPad - 40, Screen.fontColorDull, Screen.smallFont)

        ingredients.forEachIndexed { i, ing ->
            drawString(ing.name, ingpad + 30, headerPad + i * spacing,
                if (ing.filled()) Screen.fontColorBold else Screen.fontColorDull, Screen.font)
            if (ing.amount > 1) {
                drawString("x${ing.amount}", ingpad + 30 + measure(ing.name) + 8, headerPad + i * spacing + 1, Screen.fontColorDull, Screen.smallFont)
            }
            if (ing.total > 0) {
                drawString(ing.total.toString(), width - ingpad + 8, headerPad + i * spacing + 1,
                    if (ing.filled()) Screen.fontColor else Screen.fontColorDull, Screen.smallFont)
            }
        }

        drawString("You have " + if (productCount < 1) "no" else productCount.toString() + " " + (if (productCount == 1) productName else productName.plural()) + ".",
            padding, height - 100, Screen.fontColorDull, Screen.smallFont)

        drawString("Back", padding + 55, height - 50, if (selection == 0) Screen.fontColorBold else Screen.fontColorDull, Screen.font)
        if (makeOK) {
            drawString(makeVerb, width - padding - 105, height - 50, if (selection == 1) Screen.fontColorBold else Screen.fontColorDull, Screen.font)
        }
    }

    override fun drawBackground() {
        super.drawBackground()
        if (isAnimating()) return

        ingredients.forEachIndexed { i, ing ->
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

        ingredients.forEachIndexed { i, ing ->
            val ix0 = x + ingpad
            val iy0 = y + headerPad + i * spacing - 12
            myThingBatch()?.also { batch ->
                batch.addPixelQuad(ix0, iy0, ix0 + 32, iy0 + 32, batch.getTextureIndex(ing.glyph), hue = ing.hue)
            }
        }
    }
}
