package world.gen.features

import actors.actors.Villager
import kotlinx.serialization.Serializable
import util.Dice
import util.Rect
import world.ChunkScratch
import world.gen.decors.Decor
import world.gen.decors.Hut
import world.quests.FetchQuest

@Serializable
class Cabin(
    private val cabinAbandoned: Boolean = false
) : Habitation(cabinAbandoned) {
    override fun order() = 0
    override fun stage() = Stage.BUILD
    override fun numberOfQuestsDesired() = if (isAbandoned) 0 else if (Dice.chance(0.2f)) 1 else 0
    override fun canBeQuestDestination() = Dice.chance(0.1f)
    override fun createQuest() = FetchQuest()
    override fun numberOfLoreHavers() = if (Dice.flip()) 1 else 0

    companion object {
        fun canBuildOn(meta: ChunkScratch) = !meta.hasFeature(Village::class)
    }

    private val flavor = if (Dice.chance(0.5f)) Flavor.HERMIT else Flavor.HUNTER
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
        val newHomeJob = hutDecor.job()

        bounds = Rect(x0 + x, y0 + y, x0 + x+width-1, y0 + y+height-1)
        carto.addTrailBlock(bounds.x0, bounds.y0, bounds.x1, bounds.y1)

        val hermit = Villager(hutDecor.bedLocations[0], flavor(), false, newHomeJob)

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
                val newJob = decor.job()
                jobs.add(newJob)
            }
        }

    }

}
