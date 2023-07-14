package things.recipes

import actors.stats.skills.Survive
import render.tilesets.Glyph
import things.StoneAxe
import things.Thing

abstract class ImprovRecipe : Recipe() {
    override fun skill() = Survive
}

object StoneAxeRecipe : ImprovRecipe() {
    override fun name() = "stone axe"
    override fun description() = "A rock chipped sharp and tied to a stick makes a somewhat usable axe."
    override fun ingredients() = listOf(Thing.Tag.STICK, Thing.Tag.ROCK)
    override fun product() = StoneAxe()
    override fun glyph() = Glyph.AXE
    override fun difficulty() = -1f
}

object StoneAxeRecipe2 : ImprovRecipe() {
    override fun name() = "stone axe"
    override fun description() = "A rock chipped sharp and tied to a length of rebar makes a somewhat usable axe."
    override fun ingredients() = listOf(Thing.Tag.REBAR, Thing.Tag.ROCK)
    override fun product() = StoneAxe()
    override fun glyph() = Glyph.AXE
    override fun difficulty() = -1f
}
