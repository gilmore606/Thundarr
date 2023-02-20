package world.gen.features

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import things.Shrine
import things.Thing
import things.Well
import util.*
import world.ChunkScratch
import world.gen.biomes.Glacier
import world.gen.biomes.Ocean
import world.gen.cartos.WorldCarto
import world.gen.decors.*
import world.gen.gardenPlantSpawns
import world.path.DistanceMap
import world.terrains.Terrain

@Serializable
class Village(
    val name: String,
    val isAbandoned: Boolean = false,
) : ChunkFeature(
    3, Stage.BUILD
) {
    companion object {
        fun canBuildOn(meta: ChunkScratch) = !meta.hasFeature(RuinedCitySite::class) && !meta.hasFeature(Volcano::class)
                && !meta.hasFeature(Lake::class) && !meta.hasFeature(Rivers::class) && !meta.hasFeature(Coastlines::class)
                && !meta.hasFeature(Highways::class) && meta.biome !in listOf(Ocean, Glacier)

        val neighborFeatures = listOf<Triple<Float, (ChunkScratch)->Boolean, (Boolean)->ChunkFeature>>(

            Triple(0.7f, { meta -> Farm.canBuildOn(meta) }, { isAbandoned -> Farm(isAbandoned) })

        )
    }

    private val fertility = Dice.float(0.3f, 1f) * if (isAbandoned) 0.3f else 1f

    @Transient private var featureBuilt = false
    @Transient private val uniqueHuts = mutableListOf<Decor>()

    override fun cellTitle() = name

    override fun doDig() {
        uniqueHuts.add(BlacksmithShop())
        uniqueHuts.add(Schoolhouse())
        uniqueHuts.add(Church())
        uniqueHuts.add(StorageShed())

        printGrid(growBlob(63, 63), x0, y0, Terrain.Type.TEMP1)
        printGrid(growBlob(52, 52), x0 + 6, y0 + 6, Terrain.Type.TEMP2)
        if (!isAbandoned) {
            forEachTerrain(Terrain.Type.TEMP2) { x, y ->
                flagsAt(x, y).add(WorldCarto.CellFlag.NO_PLANTS)
            }
        }
        when (Dice.oneTo(4)) {
            1,2 -> layoutVillageBag()
            3 -> layoutVillageHoriz()
            4 -> layoutVillageVert()
        }
        if (!isAbandoned && Dice.chance(0.7f)) {
            placeInMostPublic(
                if (Dice.chance(0.7f)) Well() else Shrine()
            )
        }
        if (isAbandoned) {
            val wear = Dice.float(0.3f, 0.9f)
            forEachCell { x,y ->
                if (Dice.chance(when (getTerrain(x, y)) {
                        Terrain.Type.TERRAIN_WOODFLOOR -> 0.03f * wear
                        Terrain.Type.TERRAIN_STONEFLOOR -> 0.02f * wear
                        Terrain.Type.TERRAIN_DIRT -> 0.04f * wear
                        Terrain.Type.TEMP2 -> 0.06f * wear
                        else -> 0f
                })) setTerrain(x, y, Terrain.Type.TEMP1)
            }
            fuzzTerrain(Terrain.Type.TEMP1, wear, listOf(Terrain.Type.TERRAIN_WOODWALL, Terrain.Type.TERRAIN_BRICKWALL, Terrain.Type.TERRAIN_WINDOWWALL))
        }
        swapTerrain(Terrain.Type.TEMP1, meta.biome.baseTerrain)
        swapTerrain(Terrain.Type.TEMP2, meta.biome.trailTerrain(x0,y0))
    }

    private fun layoutVillageVert() {
        val xMid = 32 + Dice.range(-4, 4)
        val xMidLeft = xMid - Dice.oneTo(3)
        val xMidRight = xMid + Dice.oneTo(3)
        var cursorY= 32
        while (cursorY > 14) {
            val width = Dice.range(9, 13)
            val height = Dice.range(9, 13)
            layoutHutOrFeature(xMidLeft - width, cursorY - height, width, height, fertility, EAST)
            cursorY -= (height + Dice.oneTo(2))
            if (Dice.chance(0.1f)) cursorY = 0
        }
        cursorY = 32
        while (cursorY < 49) {
            val width = Dice.range(9, 13)
            val height = Dice.range(9, 13)
            layoutHutOrFeature(xMidLeft - width, cursorY, width, height, fertility, EAST)
            cursorY += height + Dice.oneTo(2)
            if (Dice.chance(0.1f)) cursorY = 64
        }
        cursorY = 32
        while (cursorY > 14) {
            val width = Dice.range(9, 13)
            val height = Dice.range(9, 13)
            layoutHutOrFeature(xMidRight + 1, cursorY - height, width, height, fertility, WEST)
            cursorY -= (height + Dice.oneTo(2))
            if (Dice.chance(0.1f)) cursorY = 0
        }
        cursorY = 32
        while (cursorY < 49) {
            val width = Dice.range(9, 13)
            val height = Dice.range(9, 13)
            layoutHutOrFeature(xMidRight + 1, cursorY, width, height, fertility, WEST)
            cursorY += height + Dice.oneTo(2)
            if (Dice.chance(0.1f)) cursorY = 64
        }
        for (iy in 3 .. 60) {
            if (!isAbandoned || Dice.chance(0.95f)) setTerrain(x0 + xMid, y0 + iy, Terrain.Type.TEMP2)
            if (!isAbandoned || Dice.chance(0.95f)) setTerrain(x0 + xMid + 1, y0 + iy, Terrain.Type.TEMP2)
        }
    }

    private fun layoutVillageHoriz() {
        val yMid = 32 + Dice.range(-4, 4)
        val yMidTop = yMid - Dice.oneTo(3)
        val yMidBottom = yMid + Dice.oneTo(3)
        var cursorX = 32
        while (cursorX > 14) {
            val width = Dice.range(9, 13)
            val height = Dice.range(9, 13)
            layoutHutOrFeature(cursorX - width, yMidTop - height, width, height, fertility, SOUTH)
            cursorX -= (height + Dice.oneTo(2))
            if (Dice.chance(0.1f)) cursorX = 0
        }
        cursorX = 32
        while (cursorX < 49) {
            val width = Dice.range(9, 13)
            val height = Dice.range(9, 13)
            layoutHutOrFeature(cursorX, yMidTop - height, width, height, fertility, SOUTH)
            cursorX += height + Dice.oneTo(2)
            if (Dice.chance(0.1f)) cursorX = 64
        }
        cursorX = 32
        while (cursorX > 14) {
            val width = Dice.range(9, 13)
            val height = Dice.range(9, 13)
            layoutHutOrFeature(cursorX - width, yMidBottom, width, height, fertility, NORTH)
            cursorX -= (height + Dice.oneTo(2))
            if (Dice.chance(0.1f)) cursorX = 0
        }
        cursorX = 32
        while (cursorX < 49) {
            val width = Dice.range(9, 13)
            val height = Dice.range(9, 13)
            layoutHutOrFeature(cursorX, yMidBottom, width, height, fertility, NORTH)
            cursorX += height + Dice.oneTo(2)
            if (Dice.chance(0.1f)) cursorX = 64
        }
        for (ix in 3 .. 60) {
            if (!isAbandoned || Dice.chance(0.95f)) setTerrain(x0 + ix, y0 + yMid, Terrain.Type.TEMP2)
            if (!isAbandoned || Dice.chance(0.95f)) setTerrain(x0 + ix, y0 + yMid + 1, Terrain.Type.TEMP2)
        }
    }

    private fun layoutHutOrFeature(x: Int, y: Int, width: Int, height: Int, fertility: Float,
                                   forceDoorDir: XY? = null) {
        if (!featureBuilt && Dice.chance(0.15f)) {
            buildVillageFeature(x, y, width, height)
            featureBuilt = true
        } else {
            buildHut(x, y, width, height, fertility + Dice.float(-0.3f, 0.3f), forceDoorDir, isAbandoned) { rooms ->
                val abandoned = isAbandoned || Dice.chance(0.05f)
                when (rooms.size) {
                    1 -> {
                        uniqueHuts.filter { it.fitsInRoom(rooms[0]) }.randomOrNull()?.also { uniqueDecor ->
                            uniqueDecor.furnish(rooms[0], carto, abandoned)
                            uniqueHuts.remove(uniqueDecor)
                        } ?: run {
                            Hut().furnish(rooms[0], carto, abandoned)
                        }
                    }
                    2 -> {
                        HutLivingRoom().furnish(rooms[0], carto, abandoned)
                        HutBedroom().furnish(rooms[1], carto, abandoned)
                    }
                }
            }
        }
    }

    private fun layoutVillageBag() {
        val hutCount = Dice.range(6, 18)
        var built = 0
        var width = Dice.range(9, 13)
        var height = Dice.range(9, 13)
        while (built < hutCount) {
            var tries = 0
            var placed = false
            while (tries < 1200 && !placed) {
                val x = Dice.range(3, 63 - width)
                val y = Dice.range(3, 63 - height)
                var clearHere = true
                for (tx in x until x+width) {
                    for (ty in y until y+height) {
                        if (getTerrain(x0+tx,y0+ty) !in listOf(Terrain.Type.TEMP1, Terrain.Type.TEMP2)) clearHere = false
                    }
                }
                if (clearHere) {
                    layoutHutOrFeature(x, y, width, height, fertility)
                    placed = true
                    built++
                }
                tries++
            }
            if (placed || Dice.chance(0.2f)) {
                if (Dice.chance(0.6f)) width -= 1
                if (Dice.chance(0.6f)) height -= 1
            }
            if (width <= 6 || height <= 6) built = hutCount
        }
    }

    private fun placeInMostPublic(thing: Thing) {
        fun placeWell(x: Int, y: Int) {
            spawnThing(x, y, thing)
            val edgeTerrain = if (isAbandoned) Terrain.Type.TERRAIN_UNDERGROWTH else
                if (Dice.flip()) Terrain.Type.TERRAIN_STONEFLOOR else Terrain.Type.TERRAIN_GRASS
            if (Dice.chance(0.7f)) {
                for (ix in -1 .. 1) {
                    for (iy in -1 .. 1) {
                        setTerrain(x+ix, y+iy, edgeTerrain)
                    }
                }
            }
        }
        val distanceMap = DistanceMap(chunk) { x,y ->
            getTerrain(x,y) != Terrain.Type.TEMP2
        }
        var placed = false
        forEachCell { x, y ->
            if (!placed && distanceMap.distanceAt(x, y) == distanceMap.maxDistance) {
                placeWell(x, y)
                placed = true
            }
        }
    }

    private fun buildVillageFeature(x: Int, y: Int, width: Int, height: Int) {
        val room = when (Dice.oneTo(3)) {
            1,2 -> Garden(fertility, meta.biome, meta.habitat)
            else -> Stage()
        }
        room.furnish(Decor.Room(Rect(x0+x+1, y0+y+1, x0+x+width-2, y0+y+height-2), listOf()), carto)
    }
}
