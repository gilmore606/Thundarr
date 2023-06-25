package world.gen.features

import actors.Villager
import kotlinx.serialization.Serializable
import util.Dice
import util.Rect
import world.ChunkScratch
import world.gen.decors.Decor
import world.gen.decors.Hut

@Serializable
class Cabin : Habitation() {
    override fun order() = 0
    override fun stage() = Stage.BUILD

    companion object {
        fun canBuildOn(meta: ChunkScratch) = !meta.hasFeature(Village::class)
    }

    private val flavor = if (Dice.chance(1f)) Flavor.HERMIT else Flavor.HUNTER
    override fun name() = "cabin"
    override fun flavor() = flavor

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

        val areaCount = Dice.oneTo(3)
        val areas = mutableListOf<Rect>()
        repeat (areaCount) {
            val aw = Dice.range(8, 20)
            val ah = Dice.range(8, 20)
            var tries = 0
            var placed = false
            while (tries < 500 && !placed) {
                val ax = Dice.range(3, 63 - aw)
                val ay = Dice.range(3, 63 - ah)
                var clearHere = true
                val aRect = Rect(x0 + ax, y0 + ay, x0 + ax + aw - 1, y0 + ay + ah -1)
                if (bounds.overlaps(aRect)) clearHere = false else {
                    areas.forEach { if (it.overlaps(aRect)) clearHere = false }
                }
                if (clearHere) {
                    areas.add(aRect)
                    placed = true
                }
                tries++
            }
        }
        val decors = mutableListOf<Decor>().apply { addAll(flavor().shopDecors) }
        areas.forEach { rect ->
            if (decors.isNotEmpty()) {
                val decor = decors.random()
                decors.remove(decor)
                decor.furnish(Decor.Room(rect), carto)
                val workArea = Villager.WorkArea(
                    decor.workAreaName(), rect, decor.workAreaComments(),
                    announceJobMsg = decor.announceJobMsg()
                )
                workAreas.add(workArea)
            }
        }

    }

}
