package things

import actors.actors.Actor
import actors.stats.skills.Survive
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.recipes.FoodRecipe
import things.recipes.Recipe
import ui.panels.Console
import util.groupByTag
import util.hasOneWhere
import util.turnsToRoughTime
import world.Entity
import world.stains.Fire


@Serializable
class Campfire : Thing(), Workbench, Fuel {
    override var fuel = 100f
    override fun flammability() = 0.7f
    override val tag = Tag.CAMPFIRE
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
    override fun canBeLitOnFire() = !isBurning()

    fun feedWith(log: Fuel, actor: Actor) {
        val bonus = Survive.bonus(actor) + Survive.resolve(actor, 0f) * 0.25f
        fuel += log.fuel * (2f + bonus * 2f)
        Console.sayAct("You feed %ii to %dd.", "%Dn feeds %ii to %dd.", actor, this, log as Entity)
        log.moveTo(null)
    }

    override fun uses(): MutableMap<UseTag, Use> {
        val uses = mutableMapOf<UseTag, Use>()
        super.uses().forEach { (k,v) ->
            uses[k] = v
        }

        uses[Thing.UseTag.SWITCH_ON] = Use("light ${name()} with survival skills", 3f,
            canDo = { actor,x,y,targ ->
                isNextTo(actor) && !isBurning() && !actor.contents().hasOneWhere { it.canLightFires() }
            },
            toDo = { actor,level,x,y ->
                trySurvivalLight(actor)
            })
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

    private fun trySurvivalLight(actor: Actor) {
        if (Survive.resolve(actor, 0f) > 0) {
            Console.sayAct("You rub two sticks together skillfully until the tinder catches and fire blossoms.",
                "%DN bends down and lights %dt.", actor, this)
            level()?.addStain(Fire(), xy().x,  xy().y)
        } else {
            Console.sayAct("You rub two sticks for a while, but no fire; you must be doing it wrong somehow.",
                "%DN bends down and messes with %dt.", actor, this)
        }
    }

}
