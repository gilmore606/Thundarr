package world.gen.features

import actors.Actor
import actors.Citizen
import actors.VillageGuard
import actors.Villager
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
import kotlin.math.max
import kotlin.math.min

@Serializable
class Village(
    val name: String,
    val isAbandoned: Boolean = false,
    val size: Int,
    val flavor: Flavor = Flavor.HUMAN
) : Stronghold() {
    override fun order() = 3
    override fun stage() = Stage.BUILD
    override fun name() = name
    override fun preventBiomeAnimalSpawns() = !isAbandoned
    override fun flavor() = flavor

    companion object {
        fun canBuildOn(meta: ChunkScratch) = !meta.hasFeature(RuinedCitySite::class) && !meta.hasFeature(Volcano::class)
                && !meta.hasFeature(Lake::class) && !meta.hasFeature(Rivers::class) && !meta.hasFeature(Coastlines::class)
                && !meta.hasFeature(Highways::class) && meta.biome !in listOf(Ocean, Glacier)
    }

    private val fertility = Dice.float(0.3f, 1f) * if (isAbandoned) 0.3f else 1f

    @Transient val guards = mutableListOf<VillageGuard>()  // only used during gen

    override fun cellTitle() = if (isAbandoned) "abandoned village" else name

    class HutSpec(val rect: Rect, val doorDir: XY) { }

    override fun doDig() {

        printGrid(growBlob(63, 63), x0, y0, Terrain.Type.TEMP1)
        printGrid(growBlob(52, 52), x0 + 6, y0 + 6, Terrain.Type.TEMP2)
        if (!isAbandoned) {
            forEachTerrain(Terrain.Type.TEMP2) { x, y ->
                flagsAt(x, y).add(WorldCarto.CellFlag.NO_PLANTS)
            }
        }
        // Lay out buildings and collect list of rooms to furnish
        val huts = when (Dice.oneTo(4)) {
            1,2 -> layoutVillageBag()
            3 -> layoutVillageHoriz()
            else -> layoutVillageVert()
        }.sortedByDescending { it.rect.area() }.toMutableList()

        // Build feature
        if (huts.size > 4 && Dice.chance(0.8f)) {
            val hut = huts.removeFirst()
            val decor = when (Dice.oneTo(3)) {
                1,2 -> Garden(fertility, meta.biome, meta.habitat)
                else -> Stage()
            }
            val rect = Rect(x0+hut.rect.x0+1,y0+hut.rect.y0+1,x0+hut.rect.x1-2,y0+hut.rect.y1-2)
            decor.furnish(Decor.Room(rect, listOf()), carto, isAbandoned)
            workAreas.add(Villager.WorkArea(decor.workAreaName(), rect, decor.workAreaComments()))
        }

        // Build all huts
        val hutRooms = mutableListOf<Decor.Room>()
        huts.forEach { hutSpec ->
            val r = hutSpec.rect
            val room = buildHut(r.x0, r.y0, (r.x1 - r.x0) + 1, (r.y1 - r.y0) + 1,
                fertility + Dice.float(-0.3f, 0.3f), hutSpec.doorDir, isAbandoned)
            hutRooms.add(room)
            carto.addTrailBlock(r.x0, r.y0, r.x1, r.y1)
        }

        // Place public feature
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
        // Add abandoned wear
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

        val idealHouseCount = max(huts.size / 2 + 1, min(3, huts.size))
        val shopCount = min(flavor.shopDecors.size, huts.size - idealHouseCount)

        // Spawn guards
        if (!isAbandoned) {
            spawnGuards()
        }

        // Build shops
        val ownerWorkAreas = mutableListOf<Villager.WorkArea>()
        val shopDecors = mutableListOf<Decor>().apply { addAll(flavor.shopDecors) }
        repeat (shopCount) {
            val hut = hutRooms.removeFirst()
            val decor = shopDecors.random()
            shopDecors.remove(decor)
            decor.furnish(hut, carto, isAbandoned)
            val signXY = if (!decor.needsOwner()) { null } else {
                (hut.doorXY!! + hut.doorDir!! + hut.doorDir.rotated())
            }
            val workArea = Villager.WorkArea(
                    decor.workAreaName(), hut.rect, decor.workAreaComments(),
                    decor.needsOwner(), signXY, decor.workAreaSignText(),
                    decor.announceJobMsg(), decor.workAreaChildOK()
                )
            workAreas.add(workArea)
            if (decor.needsOwner()) ownerWorkAreas.add(workArea)
        }

        // Build houses
        repeat (hutRooms.size) {
            val hut = hutRooms.removeFirst()
            val hutDecor = Hut()
            hutDecor.furnish(hut, carto, isAbandoned)
            if (!isAbandoned) {
                val newHomeArea = Villager.WorkArea(
                    "home", hut.rect, flavor().homeComments)
                var isHeadOfHousehold = true
                val familyIDs = mutableListOf<String>()
                val family = mutableListOf<Villager>()
                hutDecor.bedLocations.forEach { bedLocation ->
                    var newJobArea: Villager.WorkArea? = null
                    var isChild = false
                    if (ownerWorkAreas.isNotEmpty()) {
                        newJobArea = ownerWorkAreas.removeFirst()
                    } else {
                        isChild = !isHeadOfHousehold && Dice.chance(flavor.childChance)
                    }
                    val citizen = Villager(bedLocation, flavor, isChild).apply {
                        factionID?.also { joinFaction(it) }
                        homeArea = newHomeArea
                        fulltimeJobArea = newJobArea
                        if (newJobArea != null) {
                            val signText = newJobArea.signText!!.replace("%n", this.name())
                            val sign = if (Dice.flip()) HighwaySign(signText) else TrailSign(signText)
                            spawnThing(newJobArea.signXY!!.x, newJobArea.signXY!!.y, sign)
                        }
                    }
                    addCitizen(citizen)
                    familyIDs.add(citizen.id)
                    family.add(citizen)
                    findSpawnPointForNPC(chunk, citizen, hut.rect)?.also { spawnPoint ->
                        citizen.spawnAt(App.level, spawnPoint.x, spawnPoint.y)
                    } ?: run { log.info("Failed to spawn citizen in ${hut.rect}")}
                    lockDoor(hut.doorXY, citizen)
                    isHeadOfHousehold = false
                }
                family.forEach { it.family.addAll(familyIDs) }
            }
        }

    }

    private fun lockDoor(xy: XY?, owner: Actor) {
        xy?.also { xy ->
            chunk.thingsAt(xy.x, xy.y).firstOrNull { it is Door }?.also { door ->
                (door as Door).lockTo(owner)
                guards.forEach { door.lockTo(it) }
            }
        }
    }

    private fun spawnGuards() {
        val numGuards = 1.coerceAtLeast((size / 4))
        val bounds = Rect(x0 + 4, y0 + 4, x1 - 4, y1 - 4)
        repeat (numGuards) {
            val guard = VillageGuard(bounds, name, flavor).apply {
                factionID?.also { joinFaction(it) }
            }
            addCitizen(guard)
            guards.add(guard)
            findSpawnPointForNPC(chunk, guard, bounds)?.also { spawnPoint ->
                guard.spawnAt(App.level, spawnPoint.x, spawnPoint.y)
            }
        }
    }

    private fun layoutVillageVert(): MutableList<HutSpec> {
        val huts = mutableListOf<HutSpec>()
        val xMid = 32 + Dice.range(-4, 4)
        val xMidLeft = xMid - Dice.oneTo(3)
        val xMidRight = xMid + Dice.oneTo(3)
        var cursorY= 32
        while (cursorY > 14 && huts.size < size) {
            val width = Dice.range(9, 13)
            val height = Dice.range(9, 13)
            huts.add(HutSpec(Rect(xMidLeft - width, cursorY - height, xMidLeft - 1, cursorY - 1), EAST))
            cursorY -= (height + Dice.oneTo(2))
            if (Dice.chance(0.1f)) cursorY = 0
        }
        cursorY = 32
        while (cursorY < 49 && huts.size < size) {
            val width = Dice.range(9, 13)
            val height = Dice.range(9, 13)
            huts.add(HutSpec(Rect(xMidLeft - width, cursorY, xMidLeft - 1, cursorY + height - 1), EAST))
            cursorY += height + Dice.oneTo(2)
            if (Dice.chance(0.1f)) cursorY = 64
        }
        cursorY = 32
        while (cursorY > 14 && huts.size < size) {
            val width = Dice.range(9, 13)
            val height = Dice.range(9, 13)
            huts.add(HutSpec(Rect(xMidRight + 1, cursorY - height, xMidRight + width, cursorY - 1), WEST))
            cursorY -= (height + Dice.oneTo(2))
            if (Dice.chance(0.1f)) cursorY = 0
        }
        cursorY = 32
        while (cursorY < 49 && huts.size < size) {
            val width = Dice.range(9, 13)
            val height = Dice.range(9, 13)
            huts.add(HutSpec(Rect(xMidRight + 1, cursorY, xMidRight + width, cursorY + height - 1), WEST))
            cursorY += height + Dice.oneTo(2)
            if (Dice.chance(0.1f)) cursorY = 64
        }
        for (iy in 3 .. 60) {
            if (!isAbandoned || Dice.chance(0.95f)) setTerrain(x0 + xMid, y0 + iy, Terrain.Type.TEMP2)
            if (!isAbandoned || Dice.chance(0.95f)) setTerrain(x0 + xMid + 1, y0 + iy, Terrain.Type.TEMP2)
        }
        return huts
    }

    private fun layoutVillageHoriz(): MutableList<HutSpec> {
        val huts = mutableListOf<HutSpec>()
        val yMid = 32 + Dice.range(-4, 4)
        val yMidTop = yMid - Dice.oneTo(3)
        val yMidBottom = yMid + Dice.oneTo(3)
        var cursorX = 32
        while (cursorX > 14 && huts.size < size) {
            val width = Dice.range(9, 13)
            val height = Dice.range(9, 13)
            huts.add(HutSpec(Rect(cursorX - width, yMidTop - height, cursorX - 1, yMidTop - 1), SOUTH))
            cursorX -= (height + Dice.oneTo(2))
            if (Dice.chance(0.1f)) cursorX = 0
        }
        cursorX = 32
        while (cursorX < 49 && huts.size < size) {
            val width = Dice.range(9, 13)
            val height = Dice.range(9, 13)
            huts.add(HutSpec(Rect(cursorX, yMidTop - height, cursorX + width - 1, yMidTop - 1), SOUTH))
            cursorX += height + Dice.oneTo(2)
            if (Dice.chance(0.1f)) cursorX = 64
        }
        cursorX = 32
        while (cursorX > 14 && huts.size < size) {
            val width = Dice.range(9, 13)
            val height = Dice.range(9, 13)
            huts.add(HutSpec(Rect(cursorX - width, yMidBottom, cursorX - 1, yMidBottom + height - 1), NORTH))
            cursorX -= (height + Dice.oneTo(2))
            if (Dice.chance(0.1f)) cursorX = 0
        }
        cursorX = 32
        while (cursorX < 49 && huts.size < size) {
            val width = Dice.range(9, 13)
            val height = Dice.range(9, 13)
            huts.add(HutSpec(Rect(cursorX, yMidBottom, cursorX + width - 1, yMidBottom + height - 1), NORTH))
            cursorX += height + Dice.oneTo(2)
            if (Dice.chance(0.1f)) cursorX = 64
        }
        for (ix in 3 .. 60) {
            if (!isAbandoned || Dice.chance(0.95f)) setTerrain(x0 + ix, y0 + yMid, Terrain.Type.TEMP2)
            if (!isAbandoned || Dice.chance(0.95f)) setTerrain(x0 + ix, y0 + yMid + 1, Terrain.Type.TEMP2)
        }
        return huts
    }

    private fun layoutVillageBag(): MutableList<HutSpec> {
        val hutCount = this.size + 1
        val huts = mutableListOf<HutSpec>()
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
                val hutRect = Rect(x, y, x + width - 1, y + height - 1)
                huts.forEach { if (it.rect.overlaps(hutRect)) clearHere = false }
                if (clearHere) {
                    huts.add(HutSpec(hutRect, CARDINALS.random()))
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
        return huts
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

    override fun mapIcon(): Glyph? = Glyph.MAP_VILLAGE
    override fun mapPOITitle() = name
    override fun mapPOIDescription() = "The ${flavor.displayName} village of $name."

}
