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
        )
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

abstract class FoodRecipe : Recipe() {
    override fun makeFailMsg() = "Eugh.  That came out completely inedible."
    override fun makeSuccessMsg() = "A little more salt, and...done!  One delicious " + product().name() + "."
}

object SteakRecipe : FoodRecipe() {
    override fun name() = "seared steak"
    override fun description() = "You can cook a delicious steak from the meat of almost anything."
    override fun ingredients() = listOf(Thing.Tag.THING_RAWMEAT)
    override fun product() = Steak()
    override fun glyph() = Glyph.MEAT
    override fun skill() = Survive
    override fun difficulty() = 1f
}

object ChickenRecipe : FoodRecipe() {
    override fun name() = "roast chicken"
    override fun description() = "Roasted chicken meat is far tastier and more nutritious."
    override fun ingredients() = listOf(Thing.Tag.THING_CHICKENLEG)
    override fun product() = Steak()
    override fun glyph() = Glyph.MEAT
    override fun skill() = Survive
    override fun difficulty() = 1f
}

object TestRecipe : FoodRecipe() {
    override fun name() = "wizard stew"
    override fun description() = "A wizard stew doesn't contain actual wizard, unfortunately.  Just mushrooms and meat."
    override fun ingredients() = listOf(Thing.Tag.THING_CHICKENLEG, Thing.Tag.THING_WIZARDCAP_MUSHROOM, Thing.Tag.THING_PEAR, Thing.Tag.THING_APPLE)
    override fun product() = Stew()
    override fun glyph() = Glyph.STEW
    override fun skill() = Survive
    override fun difficulty() = -5f
}
