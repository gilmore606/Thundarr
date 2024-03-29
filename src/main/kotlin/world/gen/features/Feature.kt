package world.gen.features

import actors.actors.NPC
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.tilesets.Glyph
import things.Thing
import things.WoodDoor
import util.*
import world.Chunk
import world.ChunkMeta
import world.gen.AnimalSpawnSource
import world.gen.biomes.Biome
import world.gen.cartos.WorldCarto
import world.gen.decors.*
import world.gen.habitats.Habitat
import world.gen.spawnsets.AnimalSet
import world.level.Level
import world.quests.Quest
import world.terrains.Terrain

@Serializable
sealed class Feature : AnimalSpawnSource {

    open fun name() = ""
    open fun order(): Int = 0
    open fun stage(): Stage = Stage.TERRAIN
    enum class Stage { TERRAIN, BUILD }

    open fun preventBiomeAnimalSpawns() = false
    override fun animalSpawnPoint(chunk: Chunk, animal: NPC, near: XY?, within: Float?): XY? = null

    open fun canBeQuestDestination() = false
    open fun createQuest(): Quest? = null

    // How close must this be to be known as part of someone's area lore?
    open fun loreKnowabilityRadius() = 0
    open fun loreName() = "something"

    open fun xpValue() = 1

    open fun temperatureMod() = 0

    open fun animalSet(meta: ChunkMeta): AnimalSet? = null
    open fun animalSpawnCount() = Dice.oneTo(3)

    var worldX = 0
    var worldY = 0

    override fun toString() = "${name()} ($worldX,$worldY)"

    @Transient lateinit var carto: WorldCarto
    @Transient lateinit var meta: ChunkMeta
    @Transient lateinit var chunk: Chunk

    var x0: Int = 0
    var y0: Int = 0
    var x1: Int = 0
    var y1: Int = 0

    open fun onRestore(level: Level) { }
    open fun onSave() { }

    private val questsToDig: MutableList<String> = mutableListOf()
    fun addQuestAsDest(quest: Quest) {
        questsToDig.add(quest.id)
    }

    open fun dig(carto: WorldCarto) {
        this.carto = carto
        this.meta = carto.meta
        this.chunk = carto.chunk
        this.x0 = carto.x0
        this.x1 = carto.x1
        this.y0 = carto.y0
        this.y1 = carto.y1

        doDig()

        questsToDig.forEach { questID ->
            App.factions.questByID(questID)?.also { quest ->
                log.info("digging for $quest in $this")
                quest.dig(this)
            }
        }
    }

    abstract fun doDig()

    open fun cellTitle(): String? = null

    open fun mapIcon(onBiome: Biome? = null): Glyph? = null
    open fun mapPOITitle(): String? = null
    open fun mapPOIDescription(): String? = null

    open fun trailDestinationChance() = 0.0f

    protected fun printGrid(blob: Array<Array<Boolean>>, x: Int, y: Int, terrain: Terrain.Type) {
        carto.printGrid(blob, x, y, terrain)
    }
    protected fun growBlob(width: Int, height: Int): Array<Array<Boolean>> =
        carto.growBlob(width, height)
    protected fun fringeTerrain(type: Terrain.Type, withType: Terrain.Type, density: Float, exclude: Terrain.Type? = null) {
        carto.fringeTerrain(type, withType, density, exclude)
    }
    protected fun varianceFuzzTerrain(type: Terrain.Type, exclude: Terrain.Type? = null) {
        carto.varianceFuzzTerrain(type, exclude)
    }
    protected fun fuzzTerrain(type: Terrain.Type, density: Float) {
        carto.fuzzTerrain(type, density)
    }
    protected fun fuzzTerrain(type: Terrain.Type, density: Float, exclude: Terrain.Type?) {
        carto.fuzzTerrain(type, density, exclude)
    }
    protected fun fuzzTerrain(type: Terrain.Type, density: Float, excludeList: List<Terrain.Type?>?) {
        carto.fuzzTerrain(type, density, excludeList)
    }
    protected fun swapTerrain(oldType: Terrain.Type, newType: Terrain.Type) {
        carto.swapTerrain(oldType, newType)
    }
    protected fun forEachCell(doThis: (x: Int, y: Int) -> Unit) {
        carto.forEachCell(doThis)
    }
    protected fun forEachTerrain(type: Terrain.Type, doThis: (x: Int, y: Int)->Unit) {
        carto.forEachTerrain(type, doThis)
    }
    protected fun forEachBiome(doThis: (x: Int, y: Int, biome: Biome)->Unit) {
        carto.forEachBiome(doThis)
    }
    protected fun neighborCount(x: Int, y: Int, type: Terrain.Type) = carto.neighborCount(x, y, type)
    protected fun neighborCount(x: Int, y: Int, match: (Terrain.Type)->Boolean) = carto.neighborCount(x, y, match)
    protected fun neighborCount(x: Int, y: Int, dirs: List<XY>, match: (x: Int, y: Int)->Boolean) = carto.neighborCount(x, y, dirs, match)
    protected fun boundsCheck(x: Int, y: Int) = (x >= x0 && y >= y0 && x <= x1 && y <= y1)
    protected fun isWalkableAt(x: Int, y: Int) = chunk.isWalkableAt(App.player, x, y)
    protected fun isRoofedAt(x: Int, y: Int) = chunk.isRoofedAt(x, y)
    protected fun getTerrain(x: Int, y: Int) = chunk.getTerrain(x, y)
    protected fun setTerrain(x: Int, y: Int, type: Terrain.Type) = chunk.setTerrain(x, y, type, Terrain.get(type).isOpaque())
    protected fun safeSetTerrain(x: Int, y: Int, type: Terrain.Type) = carto.safeSetTerrain(x, y, type)
    protected fun spawnThing(x: Int, y: Int, thing: Thing) {
        carto.spawnThing(x, y, thing)
    }
    protected fun biomeAt(x: Int, y: Int) = carto.biomeAt(x, y)

    protected fun flagsAt(x: Int, y: Int) = carto.flagsMap[x - x0][y - y0]

    protected fun carveBlock(x0: Int, y0: Int, x1: Int, y1: Int, terrain: Terrain.Type) {
        forXY(x0,y0, x1,y1) { x,y ->
            setTerrain(x, y, terrain)
        }
    }

    protected fun carveFlowBlob(room: Rect,
                                type: Terrain.Type = Terrain.Type.TERRAIN_STONEFLOOR,
                                skipCorners: Boolean = false, skipTerrain: Terrain.Type? = null) {
        forXY(room) { x,y ->
            if (x >= x0 && y >= y0 && x <= x1 && y <= y1) {
                if (!skipCorners || !((x == x0 || x == x1) && (y == y0 || y == y1))) {
                    if (!flagsAt(x,y).contains(WorldCarto.CellFlag.OCEAN) && !flagsAt(x,y).contains(WorldCarto.CellFlag.BEACH)) {
                        if (skipTerrain == null || getTerrain(x, y) != skipTerrain) {
                            setTerrain(x, y, type)
                            flagsAt(x,y).apply {
                                add(WorldCarto.CellFlag.NO_PLANTS)
                                add(WorldCarto.CellFlag.NO_BUILDINGS)
                            }
                        }
                    }
                }
            }
        }
    }

    protected fun buildHut(x: Int, y: Int, width: Int, height: Int, fertility: Float,
                           forceDoorDir: XY? = null, isAbandoned: Boolean = false,
                           hasWindows: Boolean = true, forceFloor: Terrain.Type? = null, forceWall: Terrain.Type? = null,
                           buildByOutsideDoor: ((x: Int, y: Int)->Unit)? = null,
                           ): Decor.Room {
        carto.addTrailBlock(x0 + x, y0 + y, x0 + x + width - 1, y0 + y + height - 1)
        val wallType = forceWall ?: meta.biome.villageWallType()
        val floorType = forceFloor ?:  meta.biome.villageFloorType()
        val dirtType =  meta.biome.bareTerrain(x0,y0)
        // Locate door
        val doorDir = forceDoorDir ?: when {
            y < 28 -> SOUTH
            x < 20 -> EAST
            x > 40 -> WEST
            else -> NORTH
        }
        val doorx = if (doorDir == NORTH || doorDir == SOUTH) {
            Dice.range(x+2, x+width-3)
        } else {
            if (doorDir == EAST) x+width-2 else x+1
        }
        val doory = if (doorDir == EAST || doorDir == WEST) {
            Dice.range(y+2, y+height-3)
        } else {
            if (doorDir == SOUTH) y+height-2 else y+1
        }

        val doorClearCell = XY(x0 + doorx - doorDir.x, y0 + doory - doorDir.y)

        val room = (Decor.Room(
            Rect(x0+x+2, y0+y+2, x0+x+width-3, y0+y+height-3),
            listOf(doorClearCell),
            XY(x0 + doorx, y0 + doory),
            doorDir
        ))
        // Draw yard/wall/floor terrain, with door and windows
        var windowBlockerCount = Dice.range(3, 10)
        for (tx in x until x+width) {
            for (ty in y until y+height) {
                if (boundsCheck(x0+tx, y0+ty)) {
                    if (tx == doorx && ty == doory) {
                        setTerrain(x0 + tx, y0 + ty, floorType)
                        if (!isAbandoned || Dice.chance(0.5f)) spawnThing(
                            x0 + tx,
                            y0 + ty,
                            WoodDoor()
                        )
                        chunk.setRoofed(x0 + tx, y0 + ty, Chunk.Roofed.WINDOW)
                    } else if (tx == x || tx == x + width - 1 || ty == y || ty == y + height - 1) {
                        safeSetTerrain(x0 + tx, y0 + ty, Terrain.Type.TEMP3)
                    } else if (tx == x + 1 || tx == x + width - 2 || ty == y + 1 || ty == y + height - 2) {
                        if (windowBlockerCount < 1 && hasWindows &&
                            ((tx > x + 1 && tx < x + width - 2) || (ty > y + 1 && ty < y + height - 2))
                        ) {
                            setTerrain(
                                x0 + tx, y0 + ty,
                                if (!isAbandoned || Dice.chance(0.5f)) Terrain.Type.TERRAIN_WINDOWWALL else floorType
                            )
                            chunk.setRoofed(x0 + tx, y0 + ty, Chunk.Roofed.WINDOW)
                            windowBlockerCount = Dice.range(4, 12)
                        } else {
                            setTerrain(x0 + tx, y0 + ty, wallType)
                            chunk.setRoofed(x0 + tx, y0 + ty, Chunk.Roofed.INDOOR)
                            windowBlockerCount--
                        }
                    } else {
                        setTerrain(x0 + tx, y0 + ty, floorType)
                        chunk.setRoofed(x0 + tx, y0 + ty, Chunk.Roofed.INDOOR)
                    }
                    flagsAt(x0 + tx, y0 + ty).add(WorldCarto.CellFlag.NO_PLANTS)
                }
            }
        }

        // Grow yard
        safeSetTerrain(x0 + doorx + doorDir.x, y0 + doory + doorDir.y, dirtType)
        fuzzTerrain(Terrain.Type.TEMP3, 0.4f, listOf(wallType, Terrain.Type.TERRAIN_WINDOWWALL))
        // Cut walkway through yard
        if (!isAbandoned || Dice.chance(0.3f)) safeSetTerrain(x0 + doorx + doorDir.x, y0 + doory + doorDir.y,
            if (Dice.chance(0.3f)) Terrain.Type.TERRAIN_STONEFLOOR else dirtType)
        if (!isAbandoned || Dice.chance(0.3f)) safeSetTerrain(x0 + doorx + doorDir.x*2, y0 + doory + doorDir.y * 2, dirtType)
        if (!isAbandoned) flagsAt(x0 + doorx + doorDir.x, y0 + doory + doorDir.y).add(WorldCarto.CellFlag.NO_PLANTS)
        buildByOutsideDoor?.also {
            it.invoke(x0 + doorx + doorDir.x*2 - doorDir.y, y0 + doory + doorDir.y*2 - doorDir.x)
        }
        // Grow plants in yard
        val plantDensity = fertility * 2f
        forEachTerrain(Terrain.Type.TEMP3) { x, y ->
            carto.getPlant(meta.biome, meta.habitat, 0.5f,
                plantDensity
            )?.also { plant ->
                spawnThing(x, y, plant)
            }
        }

        swapTerrain(Terrain.Type.TEMP3, meta.biome.baseTerrain)

        return room
    }

    protected fun findSpawnPointForNPCType(chunk: Chunk, animalType: NPC.Tag, bounds: Rect): XY? {
        val goat = NPC.create(animalType)
        return findSpawnPointForNPC(chunk, goat, bounds)
    }

    protected fun findSpawnPointForNPC(chunk: Chunk, npc: NPC, bounds: Rect): XY? {
        var tries = 0
        while (tries < 500) {
            tries++
            val x = Dice.range(bounds.x0, bounds.x1)
            val y = Dice.range(bounds.y0, bounds.y1)
            if (chunk.isWalkableAt(npc, x, y) && npc.canSpawnAt(chunk, x, y)) return XY(x,y)
        }
        return null
    }

    protected fun findWalkablePoint(rect: Rect, filter:((XY)->Boolean)? = null) = findWalkablePoint(rect.x0, rect.y0, rect.x1, rect.y1, filter)
    protected fun findWalkablePoint(x0: Int, y0: Int, x1: Int, y1: Int, filter: ((XY)->Boolean)? = null): XY? {
        var tries = 0
        while (tries < 400) {
            val tx = Dice.range(x0, x1)
            val ty = Dice.range(y0, y1)
            if (boundsCheck(tx, ty) && isWalkableAt(tx, ty)) {
                if (filter == null) return XY(tx,ty)
                if (filter.invoke(XY(tx, ty))) return XY(tx, ty)
            }
            tries++
        }
        return null
    }
}
