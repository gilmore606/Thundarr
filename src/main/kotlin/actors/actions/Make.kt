package actors.actions

import actors.actors.Actor
import actors.actors.Player
import kotlinx.serialization.Serializable
import render.Screen
import things.Thing
import things.Workbench
import things.recipes.Recipe
import ui.modals.RecipeModal
import ui.panels.Console
import world.level.Level

@Serializable
class Make(
    private val makeDuration: Float = 1f,
    val recipeTag: Recipe.Tag,
    val components: List<Thing>,
) : Action(makeDuration) {
    override fun name() = "make something"

    override fun execute(actor: Actor, level: Level) {
        val recipe = recipeTag.get
        val bonus = recipe.difficulty()
        val roll = recipe.skill().resolve(actor, bonus)
        if (roll >= 0) {
            doMake(actor, level)
        } else if (roll < -5) {
            wasteComponent(actor, level)
            doFail(actor, level)
        } else {
            doFail(actor, level)
        }

        Screen.topModal?.also {
            if (it is RecipeModal) it.onMakeFinish()
        }
    }

    private fun doMake(actor: Actor, level: Level) {
        components.forEach { it.moveTo(null) }
        Console.say(recipeTag.get.makeSuccessMsg())
        recipeTag.get.product().spawnTo(actor)
    }

    private fun wasteComponent(actor: Actor, level: Level) {
        components.random().also {
            if (actor is Player) Console.say("In your fumbling, you ruined ${it.iname()}.")
            it.moveTo(null)
        }
    }

    private fun doFail(actor: Actor, level: Level) {
        Console.say(recipeTag.get.makeFailMsg())
    }
}
