package things.recipes

import actors.stats.Stat
import render.tilesets.Glyph
import things.Thing

abstract class Recipe {
    companion object {
        val all = listOf(
            SteakRecipe,
            ChickenRecipe,
            StewRecipe,
            StoneAxeRecipe,
        )
        val improv = listOf(
            SteakRecipe,
            ChickenRecipe,
            StewRecipe,
            StoneAxeRecipe,
        )

        fun recipesFor(ingredient: Thing, improvOnly: Boolean = false): Set<Recipe> = mutableSetOf<Recipe>().apply {
            (if (improvOnly) improv else all).forEach { recipe ->
                if (recipe.canUse(ingredient)) add(recipe)
            }
        }
    }

    enum class Tag(
        val recipeName: String,
        val get: Recipe
    ) {
        STONE_AXE("stone axe", StoneAxeRecipe),
        STEAK("steak", SteakRecipe),
        CHICKEN("roast chicken", ChickenRecipe),
        STEW("stew", StewRecipe),
    }

    abstract fun tag(): Tag

    class Ingredient(
        val description: String? = null,
        val tag: Thing.Tag? = null,
        val tags: Set<Thing.Tag>? = null,
        val qualifier: ((Thing)->Boolean)? = null,
        val amount: Int = 1,
        val glyph: Glyph? = null,
    ) {
        fun matches(thing: Thing): Boolean {
            if (thing.tag == tag) return true
            if (tags?.contains(thing.tag) == true) return true
            if (qualifier?.invoke(thing) == true) return true
            return false
        }
        fun description(): String {
            val desc = description ?:
                tag?.let { if (amount > 1) it.pluralName else it.singularName } ?:
                tags?.let { (if (amount > 1) it.map { it.pluralName } else it.map { it.singularName }).joinToString(" or ") } ?:
                "???"
            return if (amount > 1) desc + " (${amount})" else desc
        }
        fun glyph(): Glyph {
            glyph?.also { return it }
            tag?.also { return tag.create().glyph() }
            tags?.also { return tags.first().create().glyph() }
            return Glyph.BLANK
        }
        fun hue(): Float {
            tag?.also { return tag.create().hue() }
            tags?.also { return tags.first().create().hue() }
            return 0f
        }
    }

    abstract fun name(): String
    open fun description(): String = ""
    abstract fun ingredients(): List<Ingredient>
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

    fun canUse(ingredient: Thing): Boolean {
        ingredients().forEach { required ->
            if (required.matches(ingredient)) return true
        }
        return false
    }
}
