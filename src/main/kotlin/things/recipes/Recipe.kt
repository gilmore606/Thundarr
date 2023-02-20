package things.recipes

import actors.stats.Stat
import actors.stats.skills.Survive
import things.Steak
import things.Thing

abstract class Recipe {
    companion object {
        val all = listOf(
            SteakRecipe
        )
    }
    abstract fun ingredients(): List<Thing.Tag>
    abstract fun product(): Thing
    abstract fun skill(): Stat
    abstract fun difficulty(): Float
    open fun makeFailMsg() = "Hmmm.  That didn't come out right."
    open fun makeSuccessMsg() = "And....done!  You made " + product().iname() + "."
    open fun makeDuration() = 5f

}

abstract class FoodRecipe : Recipe() {
    override fun makeFailMsg() = "Eugh.  That came out completely inedible."
    override fun makeSuccessMsg() = "A little more salt, and...done!  One delicious " + product().name() + "."
}

object SteakRecipe : FoodRecipe() {
    override fun ingredients() = listOf(Thing.Tag.THING_RAWMEAT)
    override fun product() = Steak()
    override fun skill() = Survive
    override fun difficulty() = 1f
}
