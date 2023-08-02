package things

import actors.actors.Actor
import render.Screen
import render.tilesets.Glyph
import things.recipes.Recipe
import ui.modals.WorkbenchModal

interface Workbench {
    fun name(): String
    fun isNextTo(actor: Actor): Boolean
    fun examineDescription(): String
    fun workbenchDescription() = examineDescription()
    fun glyph(): Glyph
    fun workbenchGlyph() = glyph()
    fun hue(): Float
    fun workbenchTitle() = name()

    fun getPossibleRecipes(crafter: Actor) {
        val recipes = mutableListOf<Recipe>()
        Recipe.all.forEach { recipe ->
            var possible = true
            val pool = mutableListOf<Thing>().apply { addAll(crafter.contents()) }
            recipe.ingredients().forEach { ingredient ->
                pool.firstOrNull { ingredient.matches(it) }?.also {
                    pool.remove(it)
                } ?: run {
                    possible = false
                }
            }
            if (possible) recipes.add(recipe)
        }
    }

    abstract fun benchCanMake(recipe: Recipe): Boolean

    fun getRecipes(): List<Recipe> = mutableListOf<Recipe>().apply {
        Recipe.all.forEach { if (it.isPublic() && benchCanMake(it) ) add(it) }
        // TODO: add recipe books
    }

    fun canCraft(crafter: Actor) = true

    fun craftVerb() = "craft"
    fun craftPreposition() = "on"
    fun ingredientWord() = "ingredient"
    fun craftUse() = Thing.Use(craftVerb() + " " + craftPreposition() + " " + name(), 1f,
        canDo = { actor, x, y, targ ->
            isNextTo(actor) && canCraft(actor)
        },
        toDo = { actor, level, x, y ->
            beginCrafting(actor)
        })

    fun beginCrafting(crafter: Actor) {
        Screen.addModal(WorkbenchModal(this))
    }
}
