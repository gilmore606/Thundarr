package world.gen.features

import actors.Citizen
import actors.VillageGuard
import actors.Villager
import actors.factions.VillageFaction
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.tilesets.Glyph
import things.*
import util.*
import world.ChunkScratch
import world.gen.biomes.Glacier
import world.gen.biomes.Ocean
import world.gen.cartos.WorldCarto
import world.gen.decors.*
import world.level.Level
import world.path.DistanceMap
import world.terrains.Terrain

@Serializable
class Village(
    val name: String,
    val isAbandoned: Boolean = false,
    val size: Int,
) : Stronghold() {
    override fun order() = 3
    override fun stage() = Stage.BUILD
    override fun name() = name
    override fun preventBiomeAnimalSpawns() = !isAbandoned

    companion object {
        fun canBuildOn(meta: ChunkScratch) = !meta.hasFeature(RuinedCitySite::class) && !meta.hasFeature(Volcano::class)
                && !meta.hasFeature(Lake::class) && !meta.hasFeature(Rivers::class) && !meta.hasFeature(Coastlines::class)
                && !meta.hasFeature(Highways::class) && meta.biome !in listOf(Ocean, Glacier)

        val neighborFeatures = listOf<Triple<Float, (ChunkScratch, Village)->Boolean, (Boolean, XY)->Feature>>(

            Triple(0.6f, { meta, village ->
                Farm.canBuildOn(meta)
                         }, { isAbandoned, dir ->
                Farm(isAbandoned)
            }),
            Triple(0.3f, { meta, village ->
                Graveyard.canBuildOn(meta)
                         }, { isAbandoned, dir ->
                Graveyard(isAbandoned)
            }),
            Triple(0.3f, { meta, village ->
                village.size > 8 && !village.isAbandoned && Tavern.canBuildOn(meta)
                         }, { isAbandoned, dir ->
                Tavern(Madlib.tavernName(), dir)
            }),

        )
    }

    private val fertility = Dice.float(0.3f, 1f) * if (isAbandoned) 0.3f else 1f

    private val citizens = mutableSetOf<String>() // actor ids
    private fun addCitizen(citizen: Citizen) {
        citizens.add(citizen.id)
        citizen.village = this
    }

    val workAreas = mutableSetOf<Villager.WorkArea>()

    @Transient private var featureBuilt = false
    @Transient private val uniqueHuts = mutableListOf<Decor>(
        BlacksmithShop(),
        Schoolhouse(),
        Church(),
        StorageShed(),
    )

    val factionID = App.factions.addFaction(VillageFaction(name))

    override fun cellTitle() = if (isAbandoned) "abandoned village" else name

    override fun trailDestinationChance() = 1f

    override fun doDig() {

        printGrid(growBlob(63, 63), x0, y0, Terrain.Type.TEMP1)
        printGrid(growBlob(52, 52), x0 + 6, y0 + 6, Terrain.Type.TEMP2)
        if (!isAbandoned) {
            forEachTerrain(Terrain.Type.TEMP2) { x, y ->
                flagsAt(x, y).add(WorldCarto.CellFlag.NO_PLANTS)
            }
        }
        // Lay out buildings and collect list of rooms to furnish
        when (Dice.oneTo(4)) {
            1,2 -> layoutVillageBag()
            3 -> layoutVillageHoriz()
            4 -> layoutVillageVert()
        }
        // Furnish all rooms
        
        if (!isAbandoned && Dice.chance(0.7f)) {
            placeInMostPublic(
                when (Dice.oneTo(10)) {
                    1 -> Gravestone()
                    2 -> Shrine()
                    3 -> if (Dice.chance(0.3f)) WreckedCar() else DeadTree()
                    else -> Well()
                }
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
        swapTerrain(Terrain.Type.TEMP2, meta.biome.bareTerrain(x0,y0))

        if (!isAbandoned) {
            spawnGuards()
        }
    }

    private fun spawnGuards() {
        val numGuards = 1.coerceAtLeast((size / 4))
        val bounds = Rect(x0 + 4, y0 + 4, x1 - 4, y1 - 4)
        repeat (numGuards) {
            val guard = VillageGuard(bounds, name).apply {
                joinFaction(factionID)
            }
            addCitizen(guard)
            findSpawnPointForNPC(chunk, guard, bounds)?.also { spawnPoint ->
                guard.spawnAt(App.level, spawnPoint.x, spawnPoint.y)
            }
        }
    }

    private fun layoutVillageVert() {
        var huts = 0
        val xMid = 32 + Dice.range(-4, 4)
        val xMidLeft = xMid - Dice.oneTo(3)
        val xMidRight = xMid + Dice.oneTo(3)
        var cursorY= 32
        while (cursorY > 14 && huts < size) {
            val width = Dice.range(9, 13)
            val height = Dice.range(9, 13)
            layoutHutOrFeature(xMidLeft - width, cursorY - height, width, height, fertility, EAST)
            huts++
            cursorY -= (height + Dice.oneTo(2))
            if (Dice.chance(0.1f)) cursorY = 0
        }
        cursorY = 32
        while (cursorY < 49 && huts < size) {
            val width = Dice.range(9, 13)
            val height = Dice.range(9, 13)
            layoutHutOrFeature(xMidLeft - width, cursorY, width, height, fertility, EAST)
            huts++
            cursorY += height + Dice.oneTo(2)
            if (Dice.chance(0.1f)) cursorY = 64
        }
        cursorY = 32
        while (cursorY > 14 && huts < size) {
            val width = Dice.range(9, 13)
            val height = Dice.range(9, 13)
            layoutHutOrFeature(xMidRight + 1, cursorY - height, width, height, fertility, WEST)
            huts++
            cursorY -= (height + Dice.oneTo(2))
            if (Dice.chance(0.1f)) cursorY = 0
        }
        cursorY = 32
        while (cursorY < 49 && huts < size) {
            val width = Dice.range(9, 13)
            val height = Dice.range(9, 13)
            layoutHutOrFeature(xMidRight + 1, cursorY, width, height, fertility, WEST)
            huts++
            cursorY += height + Dice.oneTo(2)
            if (Dice.chance(0.1f)) cursorY = 64
        }
        for (iy in 3 .. 60) {
            if (!isAbandoned || Dice.chance(0.95f)) setTerrain(x0 + xMid, y0 + iy, Terrain.Type.TEMP2)
            if (!isAbandoned || Dice.chance(0.95f)) setTerrain(x0 + xMid + 1, y0 + iy, Terrain.Type.TEMP2)
        }
    }

    private fun layoutVillageHoriz() {
        var huts = 0
        val yMid = 32 + Dice.range(-4, 4)
        val yMidTop = yMid - Dice.oneTo(3)
        val yMidBottom = yMid + Dice.oneTo(3)
        var cursorX = 32
        while (cursorX > 14 && huts < size) {
            val width = Dice.range(9, 13)
            val height = Dice.range(9, 13)
            layoutHutOrFeature(cursorX - width, yMidTop - height, width, height, fertility, SOUTH)
            huts++
            cursorX -= (height + Dice.oneTo(2))
            if (Dice.chance(0.1f)) cursorX = 0
        }
        cursorX = 32
        while (cursorX < 49 && huts < size) {
            val width = Dice.range(9, 13)
            val height = Dice.range(9, 13)
            layoutHutOrFeature(cursorX, yMidTop - height, width, height, fertility, SOUTH)
            huts++
            cursorX += height + Dice.oneTo(2)
            if (Dice.chance(0.1f)) cursorX = 64
        }
        cursorX = 32
        while (cursorX > 14 && huts < size) {
            val width = Dice.range(9, 13)
            val height = Dice.range(9, 13)
            layoutHutOrFeature(cursorX - width, yMidBottom, width, height, fertility, NORTH)
            huts++
            cursorX -= (height + Dice.oneTo(2))
            if (Dice.chance(0.1f)) cursorX = 0
        }
        cursorX = 32
        while (cursorX < 49 && huts < size) {
            val width = Dice.range(9, 13)
            val height = Dice.range(9, 13)
            layoutHutOrFeature(cursorX, yMidBottom, width, height, fertility, NORTH)
            huts++
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
            var withCitizen = false
            var areaName = "home"
            var areaComments = setOf(
                "Ah, home and hearth.",
                "It's good to be home.",
                "It's not much, but it's my safe place.",
            )

            val room = buildHut(x, y, width, height, fertility + Dice.float(-0.3f, 0.3f), forceDoorDir, isAbandoned)
            val abandoned = isAbandoned || Dice.chance(0.05f)
            uniqueHuts.filter { it.fitsInRoom(room) }.randomOrNull()?.also { uniqueDecor ->
                uniqueDecor.furnish(room, carto, abandoned)
                uniqueHuts.remove(uniqueDecor)
                areaName = uniqueDecor.workAreaName()
                areaComments = uniqueDecor.workAreaComments()
            } ?: run {
                Hut().furnish(room, carto, abandoned)
                withCitizen = true
            }
            val interiorRect = Rect(x0+x+2, y0+y+2, x0+x+width-3, y0+y+height-3)
            val workArea = Villager.WorkArea(
                areaName, interiorRect, areaComments
            )
            if (withCitizen && !isAbandoned) {
                val citizen = Villager().apply {
                    joinFaction(factionID)
                    homeArea = workArea
                }
                addCitizen(citizen)
                findSpawnPointForNPC(chunk, citizen, interiorRect)?.also { spawnPoint ->
                    citizen.spawnAt(App.level, spawnPoint.x, spawnPoint.y)
                }
            } else {
                workAreas.add(workArea)
            }

        }
        carto.addTrailBlock(x, y, x+width-1, y+height-1)
    }

    private fun layoutVillageBag() {
        val hutCount = this.size
        var built = 0
        var width = Dice.range(9, 13)
        var height = Dice.range(9, 13)
        while (built < hutCount) {
            var tries = 0
            var placed = false
            while (tries < 2000 && !placed) {
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
        val distanceMap = DistanceMap(chunk, { x,y ->
            getTerrain(x,y) != Terrain.Type.TEMP2
        }, { x, y ->
            isWalkableAt(x,y)
        })
        var placed = false
        forEachCell { x, y ->
            if (!placed && distanceMap.distanceAt(x, y) == distanceMap.maxDistance) {
                placeWell(x, y)
                workAreas.add(Villager.WorkArea(
                    "town square",
                        Rect(x-2, y-2, x+2, y+2),
                    mutableSetOf(
                        "Chop wood, carry water.",
                    )
                ))
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
        workAreas.add(Villager.WorkArea(
            room.workAreaName(),
            Rect(x0+x+1, y0+y+1, x0+x+width-2, y0+y+height-2),
            room.workAreaComments()
        ))
    }

    override fun mapIcon(): Glyph? = Glyph.MAP_VILLAGE
    override fun mapPOITitle() = name
    override fun mapPOIDescription() = "The free human village of $name."

    override fun onRestore(level: Level) {
        citizens.forEach { citizenID ->
            level.director.getActor(citizenID)?.also { citizen ->
                if (citizen is Citizen) {
                    citizen.village = this
                }
            }
        }
    }

}
