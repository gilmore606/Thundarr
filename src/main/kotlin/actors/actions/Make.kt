package actors.actions

import actors.Actor
import actors.Player
import things.Workbench
import things.recipes.Recipe
import ui.panels.Console
import world.level.Level

class Make(
    private val bench: Workbench,
    private val recipe: Recipe
) : Action(recipe.makeDuration()) {
    override fun name() = "make something"

    override fun execute(actor: Actor, level: Level) {
        val roll = recipe.skill().resolve(actor, recipe.difficulty())
        if (roll < 0f) {
            if (actor is Player) Console.say(recipe.makeFailMsg())
            if (roll < -2f) {
                recipe.ingredients().random().also { thingTag ->
                    bench.contents().firstOrNull { it.thingTag() == thingTag }?.moveTo(null)
                }
            } else if (roll < -5f) {
                consumeAllIngredients()
            }
        } else {
            if (actor is Player) Console.say(recipe.makeSuccessMsg())
            consumeAllIngredients()
            recipe.product().moveTo(bench)
        }
    }

    private fun consumeAllIngredients() {
        recipe.ingredients().forEach { thingTag ->
            bench.contents().firstOrNull { it.thingTag() == thingTag }?.moveTo(null)
        }
    }
}
