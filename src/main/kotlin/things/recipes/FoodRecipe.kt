package things.recipes

import actors.stats.skills.Survive
import render.tilesets.Glyph
import things.Steak
import things.Stew
import things.Thing


abstract class FoodRecipe : Recipe() {
    override fun makeFailMsg() = "Eugh.  That came out completely inedible."
    override fun makeSuccessMsg() = "A little more salt, and...done!  One delicious " + product().name() + "."
}

object SteakRecipe : FoodRecipe() {
    override fun name() = "seared steak"
    override fun description() = "You can cook a delicious steak from the meat of almost anything."
    override fun ingredients() = listOf(Thing.Tag.RAWMEAT)
    override fun product() = Steak()
    override fun glyph() = Glyph.MEAT
    override fun skill() = Survive
    override fun difficulty() = 1f
}

object ChickenRecipe : FoodRecipe() {
    override fun name() = "roast chicken"
    override fun description() = "Roasted chicken meat is far tastier and more nutritious."
    override fun ingredients() = listOf(Thing.Tag.CHICKENLEG)
    override fun product() = Steak()
    override fun glyph() = Glyph.MEAT
    override fun skill() = Survive
    override fun difficulty() = 1f
}

object TestRecipe : FoodRecipe() {
    override fun name() = "wizard stew"
    override fun description() = "A wizard stew doesn't contain actual wizard, unfortunately.  Just mushrooms and meat."
    override fun ingredients() = listOf(Thing.Tag.CHICKENLEG, Thing.Tag.WIZARDCAP_MUSHROOM, Thing.Tag.PEAR, Thing.Tag.APPLE)
    override fun product() = Stew()
    override fun glyph() = Glyph.STEW
    override fun skill() = Survive
    override fun difficulty() = -5f
}
