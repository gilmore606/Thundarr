package world.gen.features

import actors.Villager
import kotlinx.serialization.Serializable
import util.*
import world.ChunkScratch
import world.gen.biomes.Plain
import world.gen.biomes.Scrub
import world.gen.biomes.Swamp
import world.gen.decors.Barn
import world.gen.decors.Decor
import world.gen.decors.Garden
import world.gen.decors.Hut
import world.terrains.Terrain

@Serializable
class Farm(
    private val isAbandoned: Boolean = false
) : Habitation() {
    override fun order() = 3
    override fun stage() = Stage.BUILD
    override fun preventBiomeAnimalSpawns() = !isAbandoned

    companion object {
        fun canBuildOn(meta: ChunkScratch) = meta.biome in listOf(Plain, Scrub, Swamp)
                && !meta.hasFeature(Rivers::class) && !meta.hasFeature(Coastlines::class) && !meta.hasFeature(Highways::class)
    }

    private val flavor = Flavor.FARM

    private val barnChance = 0.7f

    override fun cellTitle() = "farm"
    override fun name() = "farm"
    override fun flavor() = flavor

    override fun doDig() {
        val width = Dice.range(35, 50)
        val height = Dice.range(35, 50)
        val x = x0 + Dice.range(5, 52 - width)
        val y = y0 + Dice.range(5, 52 - height)
        carveBlock(x, y, x + width - 1, y + height - 1, Terrain.Type.TEMP1)
        repeat(3) { fuzzTerrain(Terrain.Type.TEMP1, 0.5f) }

        val field = Rect(x + Dice.range(12, 18), y + Dice.range(12, 18),
            x + width - Dice.range(1, 10), y + height - Dice.range(1, 5))
        val gardenDecor = Garden(0.5f, meta.biome, meta.habitat, Terrain.Type.TERRAIN_DIRT)
        gardenDecor.furnish(Decor.Room(field), carto, isAbandoned)
        carto.addTrailBlock(field.x0, field.y0, field.x1, field.y1)
        val gardenArea = Villager.WorkArea("fields", field, gardenDecor.workAreaComments())
        workAreas.add(gardenArea)

        var barnRect = Rect(0,0,0,0)
        if (Dice.chance(barnChance)) {
            val barnWidth = Dice.range(7, 11)
            val barnHeight = Dice.range(7, 11)
            barnRect = when (CARDINALS.random()) {
                NORTH -> {
                    val barnx0 = field.x0 + Dice.range(-3, 18)
                    val barny0 = field.y0 - barnHeight - Dice.range(2, 4)
                    Rect(barnx0, barny0, barnx0 + barnWidth - 1, barny0 + barnHeight - 1)
                }
                SOUTH -> {
                    val barnx0 = field.x0 + Dice.range(-3, 18)
                    val barny0 = field.y1 + Dice.range(2, 4)
                    Rect(barnx0, barny0, barnx0 + barnWidth - 1, barny0 + barnHeight - 1)
                }
                WEST -> {
                    val barnx0 = field.x0 - barnWidth - Dice.range(2, 4)
                    val barny0 = field.y0 + Dice.range(-3, 18)
                    Rect(barnx0, barny0, barnx0 + barnWidth - 1, barny0 + barnHeight - 1)
                }
                else -> {
                    val barnx0 = field.x1 + Dice.range(2, 4)
                    val barny0 = field.y0 + Dice.range(-3, 18)
                    Rect(barnx0, barny0, barnx0 + barnWidth - 1, barny0 + barnHeight - 1)
                }
            }
            val room = buildHut(
                barnRect.x0 - x0, barnRect.y0 - y0, barnWidth, barnHeight, 0.5f,
                isAbandoned = isAbandoned, hasWindows = false, forceFloor = Terrain.Type.TERRAIN_DIRT
            )
            val decor = Barn()
            decor.furnish(room, carto, isAbandoned)
            val barnArea = Villager.WorkArea("barn", room.rect, decor.workAreaComments())
            workAreas.add(barnArea)
        }

        val hw = Dice.range(6, 10)
        val hh = Dice.range(6, 10)
        var tries = 0
        var clearHere = false
        var hRect: Rect? = null
        while (tries < 500 && !clearHere) {
            val hx = Dice.range(3, 60 - hw)
            val hy = Dice.range(3, 60 - hh)
            clearHere = true
            hRect = Rect(hx, hy, hx + hw -1,  hy +hh -1)
            if (field.overlaps(hRect)) clearHere = false
            else if (barnRect.overlaps(hRect)) clearHere = false
            tries++
        }
        hRect?.also { hRect ->
            val hut = buildHut(hRect.x0, hRect.y0, hRect.width(), hRect.height(), 1f, isAbandoned = isAbandoned)
            val houseDecor = Hut()
            houseDecor.furnish(hut, carto, isAbandoned = isAbandoned)
            if (!isAbandoned) {
                val newHomeArea = Villager.WorkArea("home", hut.rect, flavor().homeComments)
                val farmer = Villager(houseDecor.bedLocations[0], flavor(), isChild = false).apply {
                    factionID?.also { joinFaction(it) }
                    homeArea = newHomeArea
                }
                addCitizen(farmer)
                findSpawnPointForNPC(chunk, farmer, hut.rect)?.also { spawnPoint ->
                    farmer.spawnAt(App.level, spawnPoint.x, spawnPoint.y)
                }
            }
        }

        swapTerrain(Terrain.Type.TEMP1, Terrain.Type.TERRAIN_GRASS)
    }

}
