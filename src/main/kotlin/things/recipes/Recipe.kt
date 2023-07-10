package things.recipes

import actors.stats.Stat
import actors.stats.skills.Survive
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Steak
import things.Stew
import things.Thing

@Serializable
abstract class Recipe {
    companion object {
        val all = listOf<Recipe>(
            SteakRecipe,
            ChickenRecipe,
            TestRecipe,
            StoneAxeRecipe,
        )
        val improv = listOf(
            StoneAxeRecipe
        )

        fun recipesFor(ingredient: Thing.Tag, improvOnly: Boolean = false): Set<Recipe> = mutableSetOf<Recipe>().apply {
            (if (improvOnly) improv else all).forEach { recipe ->
                if (recipe.ingredients().contains(ingredient)) add(recipe)
            }
        }
    }

    abstract fun name(): String
    open fun description(): String = ""
    abstract fun ingredients(): List<Thing.Tag>
    abstract fun product(): Thing
    abstract fun glyph(): Glyph
    abstract fun skill(): Stat
    abstract fun difficulty(): Float
    open fun isPublic() = true
    open fun makeFailMsg() = "Hmmm.  That didn't come out right."
    open fun makeSuccessMsg() = "And....done!  You made " + product().iname() + "."
    open fun makeDuration() = 5f

    fun describeDifficulty(): String {
        val diff = difficulty() + skill().get(App.player) - 10f
        val ds = (if (diff >= 0f) "+" else "") + diff.toInt().toString()
        return "($ds)"
    }
}
