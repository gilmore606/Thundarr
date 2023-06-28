package world.gen.features

import actors.Villager
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Door
import things.HighwaySign
import things.WoodDoor
import util.*
import world.ChunkScratch
import world.gen.biomes.Glacier
import world.gen.biomes.Ocean
import world.gen.decors.Barracks
import world.gen.decors.Decor
import world.gen.decors.Tavern
import world.gen.decors.TavernLoiterArea
import world.terrains.Terrain

@Serializable
class Tavern(
    private val name: String,
    private val villageDirection: XY,
    private val tavernAbandoned: Boolean = false
) : Habitation(tavernAbandoned) {
    override fun order() = 4
    override fun stage() = Stage.BUILD
    override fun numberOfQuestsDesired() = if (isAbandoned) 0 else Dice.oneTo(3)

    companion object {
        fun canBuildOn(meta: ChunkScratch) = meta.biome !in listOf(Ocean, Glacier)
    }

    override fun trailDestinationChance() = 1f
    override fun mapIcon() = Glyph.MAP_BUILDING
    override fun mapPOITitle() = name
    override fun mapPOIDescription() = "A roadside inn."

    override fun name() = name
    override fun flavor() = Flavor.TAVERN

    override fun doDig() {
        val width = Dice.range(10, 15)
        val height = Dice.range(10, 15)
        val x = x0 + Dice.range(25, 35 - width) - villageDirection.x * 14
        val y = y0 + Dice.range(25, 35 - height) - villageDirection.y * 14
        carveBlock(x-1, y-1, x + width, y + height, Terrain.Type.TEMP1)
        repeat(3) { fuzzTerrain(Terrain.Type.TEMP1, 0.4f) }
        swapTerrain(Terrain.Type.TEMP1, meta.biome.bareTerrain(x, y))

        val hut = buildHut(x-x0, y-y0, width, height, 0.3f, hasWindows = true)
        chunk.thingsAt(hut.doorXY!!.x, hut.doorXY!!.y).filterIsInstance<Door>().firstOrNull()?.also { door ->
            door.moveTo(null)
        }
        val tavernRect: Rect
        val bedroomRect: Rect
        val loiterRect: Rect

        var wallx0 = hut.rect.x0
        var wally0 = hut.rect.y0
        var wallx1 = hut.rect.x1
        var wally1 = hut.rect.y1
        var wallxinc = 0
        var wallyinc = 0
        if (hut.doorDir == EAST) {
            wallx0 = hut.rect.x0 + 3
            wallx1 = wallx0
            wallyinc = 1
            tavernRect = Rect(hut.rect.x0 + 4, hut.rect.y0, hut.rect.x1, hut.rect.y1)
            bedroomRect = Rect(hut.rect.x0, hut.rect.y0, hut.rect.x0 + 2, hut.rect.y1)
            loiterRect = Rect(hut.rect.x1 + 2, hut.rect.y0 + 1, hut.rect.x1 + 4, hut.rect.y1 - 1)
        } else if (hut.doorDir == WEST) {
            wallx0 = hut.rect.x1 - 3
            wallx1 = wallx0
            wallyinc = 1
            tavernRect = Rect(hut.rect.x0, hut.rect.y0, hut.rect.x1 - 4, hut.rect.y1)
            bedroomRect = Rect(hut.rect.x1 - 2, hut.rect.y0, hut.rect.x1, hut.rect.y1)
            loiterRect = Rect(hut.rect.x0 - 4, hut.rect.y0 + 1, hut.rect.x0 - 2, hut.rect.y1 - 1)
        } else if (hut.doorDir == NORTH) {
            wally0 = hut.rect.y1 - 3
            wally1 = wally0
            wallxinc = 1
            tavernRect = Rect(hut.rect.x0, hut.rect.y0, hut.rect.x1, hut.rect.y1 - 4)
            bedroomRect = Rect(hut.rect.x0, hut.rect.y1 -2, hut.rect.x1, hut.rect.y1)
            loiterRect = Rect(hut.rect.x0 + 1, hut.rect.y0 - 4, hut.rect.x1 -1, hut.rect.y0 - 2)
        } else {
            wally0 = hut.rect.y0 + 3
            wally1 = wally0
            wallxinc = 1
            tavernRect = Rect(hut.rect.x0, hut.rect.y0 + 4, hut.rect.x1, hut.rect.y1)
            bedroomRect = Rect(hut.rect.x0, hut.rect.y0, hut.rect.x1, hut.rect.y0 + 2)
            loiterRect = Rect(hut.rect.x0 + 1, hut.rect.y1 + 2, hut.rect.x1 -1, hut.rect.y1 + 4)
        }
        var doored = false
        while (wallx0 <= wallx1 && wally0 <= wally1) {
            if (!doored) {
                carto.setTerrain(wallx0, wally0, meta.biome.villageFloorType())
                spawnThing(wallx0, wally0, WoodDoor())
                doored = true
            } else {
                carto.setTerrain(wallx0, wally0, meta.biome.villageWallType())
            }
            wallx0 += wallxinc
            wally0 += wallyinc
        }
        val tavernHut = Decor.Room(tavernRect)
        val bedroomHut = Decor.Room(bedroomRect)
        val loiterHut = Decor.Room(loiterRect)

        val tavernDecor = Tavern(name)
        tavernDecor.furnish(tavernHut, carto)
        val signXY = (hut.doorXY!! + hut.doorDir!! + hut.doorDir.rotated())
        val tavernArea = Villager.WorkArea("common room", tavernRect, tavernDecor.workAreaComments(),
            tavernDecor.needsOwner(), signXY, tavernDecor.workAreaSignText(), tavernDecor.announceJobMsg())
        workAreas.add(tavernArea)
        val sign = HighwaySign(name)
        spawnThing(signXY.x, signXY.y, sign)

        val loiterDecor = TavernLoiterArea(name)
        loiterDecor.furnish(loiterHut, carto)
        val loiterArea = Villager.WorkArea("smoking area", loiterRect, loiterDecor.workAreaComments(),
            announceJobMsg = loiterDecor.announceJobMsg())
        workAreas.add(loiterArea)

        val bedroomDecor = Barracks(hut.doorDir in setOf(EAST, WEST))
        bedroomDecor.furnish(bedroomHut, carto)
        val bedArea = Villager.WorkArea("bunkhouse", bedroomRect, bedroomDecor.workAreaComments(),
            announceJobMsg = bedroomDecor.announceJobMsg())

        val innkeeper = Villager(bedroomDecor.bedLocations[0], Flavor.INNKEEPER)
        placeCitizen(innkeeper, tavernRect, bedArea, tavernArea)

        repeat (Dice.range(2, 5)) { n ->
            val drunk = Villager(bedroomDecor.bedLocations[n+1], flavor())
            placeCitizen(drunk, bedroomRect, bedArea)
        }

    }

}
