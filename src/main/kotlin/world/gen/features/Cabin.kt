package world.gen.features

import actors.Villager
import kotlinx.serialization.Serializable
import util.Dice
import util.Rect
import world.ChunkScratch
import world.gen.decors.Hut

@Serializable
class Cabin : Habitation() {
    override fun order() = 0
    override fun stage() = Stage.BUILD

    companion object {
        fun canBuildOn(meta: ChunkScratch) = !meta.hasFeature(Village::class)
    }


    override fun name() = "cabin"
    override fun flavor() = Flavor.HERMIT

    var bounds = Rect(0, 0, 0, 0)

    override fun doDig() {
        val width = Dice.range(6, 10)
        val height = Dice.range(6, 10)
        val x = Dice.range(3, 63 - width)
        val y = Dice.range(3, 63 - height)
        val fertility = if (Dice.chance(0.3f)) 0f else Dice.float(0.2f, 1f)
        val hut = buildHut(x, y, width, height, fertility)
        val hutDecor = Hut()
        hutDecor.furnish(hut, carto)
        val newHomeArea = Villager.WorkArea("home", hut.rect, flavor().homeComments)

        bounds = Rect(x0 + x, y0 + y, x0 + x+width-1, y0 + y+height-1)
        carto.addTrailBlock(bounds.x0, bounds.y0, bounds.x1, bounds.y1)

        val hermit = Villager(hutDecor.bedLocations[0], flavor(), false).apply {
            factionID?.also { joinFaction(it) }
            homeArea = newHomeArea
        }
        addCitizen(hermit)
        findSpawnPointForNPC(chunk, hermit, hut.rect)?.also { spawnPoint ->
            hermit.spawnAt(App.level, spawnPoint.x, spawnPoint.y)
        }
    }

}
