package actors.actions

import actors.Actor
import actors.Player
import kotlinx.serialization.Serializable
import things.Thing
import things.Workbench
import things.recipes.Recipe
import ui.panels.Console
import world.level.Level

@Serializable
class Make(
    private val benchKey: Thing.Key,
    private val recipe: Recipe
) : Action(recipe.makeDuration()) {
    override fun name() = "make something"

    override fun execute(actor: Actor, level: Level) {
        benchKey.getThing(level)?.also { bench ->
//            if (bench is Workbench) {
//                val roll = recipe.skill().resolve(actor, recipe.difficulty())
//                if (roll < 0f) {
//                    if (actor is Player) Console.say(recipe.makeFailMsg())
//                    if (roll < -2f) {
//                        recipe.ingredients().random().also { thingTag ->
//                            bench.contents().firstOrNull { it.tag == thingTag }?.moveTo(null)
//                        }
//                    } else if (roll < -5f) {
//                        consumeAllIngredients(bench)
//                    }
//                } else {
//                    if (actor is Player) Console.say(recipe.makeSuccessMsg())
//                    consumeAllIngredients(bench)
//                    recipe.product().moveTo(bench)
//                }
//            }
        }
    }

    private fun consumeAllIngredients(bench: Workbench) {
//            recipe.ingredients().forEach { thingTag ->
//                bench.contents().firstOrNull { it.tag == thingTag }?.moveTo(null)
//            }
    }
}
