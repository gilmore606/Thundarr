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
import world.stains.Fire

interface Workbench {
    fun name(): String
    fun isNextTo(actor: Actor): Boolean

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

@Serializable
class Campfire : Thing(), Workbench, Fuel {
    override var fuel = 100f
    override fun flammability() = 0.7f
    override val tag = Tag.THING_CAMPFIRE
    override fun name() = "campfire"
    override fun description() = "Logs expertly stacked for controlled burning."
    override fun examineInfo() = "The stack of wood looks like it'll burn for " + (fuel / 2f).turnsToRoughTime() + "."
    override fun craftVerb() = "cook"
    override fun glyph() = Glyph.CAMPFIRE
    override fun isPortable() = false
    override fun fuelPerTurn() = 2f
    override fun weight() = fuel / 60f
    override fun onBurn(delta: Float): Float { return super<Fuel>.onBurn(delta) }

    override fun canCraft(crafter: Actor) = fuel > 0f && isBurning()
    override fun benchCanMake(recipe: Recipe) = recipe is FoodRecipe

    fun isBurning(): Boolean = level()?.stainsAt(xy().x, xy().y)?.hasOneWhere { it is Fire } ?: false

    fun feedWith(log: Fuel, actor: Actor) {
        val bonus = Survive.bonus(actor) + Survive.resolve(actor, 0f) * 0.25f
        fuel += log.fuel * (2f + bonus * 2f)
        Console.sayAct("You feed %ii to %dd.", "%Dn feeds %ii to %dd.", actor, this, log as Entity)
        log.moveTo(null)
    }

    override fun uses(): Map<UseTag, Use> {
        val uses = mutableMapOf<UseTag, Use>()
        super.uses().forEach { (k,v) ->
            uses[k] = v
        }

        uses[Thing.UseTag.USE] = craftUse()

        var useTagNum = 0
        App.player.contents.groupByTag().forEach { group ->
            if (group[0] is Fuel) {
                val fuel = group[0] as Fuel
                uses[Use.enumeratedTag(useTagNum)] = Use("feed ${name()} with ${fuel.name()}", 1f,
                    canDo = { actor, x, y, targ ->
                        isNextTo(actor)
                    },
                    toDo = { actor, level, x, y, ->
                        feedWith(fuel, actor)
                    })
                useTagNum++
            }
        }
        return uses
    }

}
