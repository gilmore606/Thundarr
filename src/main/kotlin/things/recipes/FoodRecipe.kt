package things.recipes

import actors.stats.skills.Survive
import render.tilesets.Glyph
import things.Consumable
import things.Steak
import things.Stew
import things.Thing


abstract class FoodRecipe : Recipe() {
    override fun makeFailMsg() = "Eugh.  That came out completely inedible."
    override fun makeSuccessMsg() = "A little more salt, and...done!  One delicious " + product().name() + "."
    override fun skill() = Survive
}

object SteakRecipe : FoodRecipe() {
    override fun tag() = Tag.STEAK
    override fun name() = "seared steak"
    override fun description() = "You can cook a delicious steak from the meat of almost anything."
    override fun ingredients() = listOf(
        Ingredient(tag = Thing.Tag.RAWMEAT)
    )
    override fun product() = Steak()
    override fun glyph() = Glyph.MEAT
    override fun difficulty() = 1f
}

object ChickenRecipe : FoodRecipe() {
    override fun tag() = Tag.CHICKEN
    override fun name() = "roast chicken"
    override fun description() = "Roasted chicken meat is far tastier and more nutritious."
    override fun ingredients() = listOf(
        Ingredient(tag = Thing.Tag.CHICKENLEG)
    )
    override fun product() = Steak()
    override fun glyph() = Glyph.MEAT
    override fun difficulty() = 1f
}

object StewRecipe : FoodRecipe() {
    override fun tag() = Tag.STEW
    override fun name() = "stew"
    override fun description() = "Meat and fruit makes a tasty stew."
    override fun ingredients() = listOf(
        Ingredient("any meat", glyph = Glyph.MEAT, qualifier = { it is Consumable && it.isMeat() }),
        Ingredient("any fruit or vegetable", glyph = Glyph.FRUIT, qualifier = { it is Consumable && (it.isFruit() || it.isVegetable()) }),
    )
    override fun product() = Stew()
    override fun glyph() = Glyph.STEW
    override fun difficulty() = -1f
}
