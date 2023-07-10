package things

import actors.Actor
import actors.stats.skills.Survive
import kotlinx.serialization.Serializable
import render.Screen
import render.tilesets.Glyph
import things.recipes.FoodRecipe
import things.recipes.Recipe
import ui.modals.WorkbenchModal
import ui.panels.Console
import util.groupByTag
import util.hasOneWhere
import util.turnsToRoughTime
import world.Entity
import world.level.Level
import world.stains.Fire

interface Workbench {
    fun name(): String
    fun isNextTo(actor: Actor): Boolean
    fun examineDescription(): String
    fun glyph(): Glyph
    fun hue(): Float

    fun getPossibleRecipes(crafter: Actor) {
        val recipes = mutableListOf<Recipe>()
        Recipe.all.forEach { recipe ->
            var possible = true
            val pool = mutableListOf<Thing>().apply { addAll(crafter.contents()) }
            recipe.ingredients().forEach { ingTag ->
                pool.firstOrNull { it.tag == ingTag }?.also {
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
