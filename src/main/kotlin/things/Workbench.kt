package things

import actors.Actor
import actors.stats.skills.Survive
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.tilesets.Glyph
import things.recipes.Recipe
import ui.panels.Console
import util.groupByTag
import util.hasOneWhere
import util.safeForEach
import util.turnsToRoughTime
import world.Entity
import world.stains.Fire

@Serializable
sealed class Workbench : Container() {

    @Transient
    private val possibleRecipes = mutableListOf<Recipe>()

    override fun isEmptyMsg() = "There's nothing on the bench."
    open fun emptyOnCloseMsg() = "You take your ingredients back out of " + dname() + "."
    override fun itemLimit() = 5
    open fun useVerb() = "make"

    fun emptyOnClose() {
        if (contents().isNotEmpty()) {
            Console.say(emptyOnCloseMsg())
            contents().safeForEach { item ->
                item.moveTo(App.player)
            }
        }
    }

    fun possibleRecipes() = possibleRecipes

    override fun onAdd(thing: Thing) {
        super.onAdd(thing)
        findPossibleRecipes()
    }

    override fun onRemove(thing: Thing) {
        super.onRemove(thing)
        findPossibleRecipes()
    }

    private fun findPossibleRecipes() {
        possibleRecipes.clear()
        Recipe.all.forEach { recipe ->
            var possible = true
            val pool = mutableListOf<Thing>().apply { addAll(contents()) }
            recipe.ingredients().forEach { ingTag ->
                pool.firstOrNull { it.thingTag() == ingTag }?.also {
                    pool.remove(it)
                } ?: run {
                    possible = false
                }
            }
            if (possible) possibleRecipes.add(recipe)
        }
    }

    override fun onRestore(holder: ThingHolder) {
        super.onRestore(holder)
        findPossibleRecipes()
    }
}

@Serializable
class Campfire : Workbench(), Fuel {
    override var fuel = 100f
    override fun flammability() = 0.7f
    override fun name() = "campfire"
    override fun description() = "Logs expertly stacked for controlled burning."
    override fun examineInfo() = "The stack of wood looks like it'll burn for " + (fuel / 2f).turnsToRoughTime() + "."
    override fun glyph() = Glyph.CAMPFIRE
    override fun isPortable() = false
    override fun fuelPerTurn() = 2f
    override fun weight() = fuel / 60f
    override fun onBurn(delta: Float): Float { return super<Fuel>.onBurn(delta) }
    override fun openVerb() = "cook on"
    override fun isEmptyMsg() = "There's nothing on the grill to cook."
    override fun emptyOnCloseMsg() = "You take your ingredients back off the campfire grill."
    override fun isOpenable() = level?.stainsAt(xy()!!.x, xy()!!.y)?.hasOneWhere { it is Fire } ?: false
    override fun itemLimit() = 4
    override fun useVerb() = "cook"

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
