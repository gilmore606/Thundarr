package world.gen.features

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import things.Table
import things.Thing
import things.WoodDoor
import util.*
import world.Chunk
import world.ChunkMeta
import world.gen.biomes.Biome
import world.gen.cartos.WorldCarto
import world.gen.villagePlantSpawns
import world.terrains.Terrain

@Serializable
sealed class ChunkFeature(
    val order: Int,
    val stage: Stage,
) {
    enum class Stage { TERRAIN, BUILD }
    @Transient
    lateinit var carto: WorldCarto
    @Transient
    lateinit var meta: ChunkMeta
    @Transient
    lateinit var chunk: Chunk
    @Transient
    var x0: Int = 0
    @Transient
    var y0: Int = 0
    @Transient
    var x1: Int = 0
    @Transient
    var y1: Int = 0


    fun dig(carto: WorldCarto) {
        this.carto = carto
        this.meta = carto.meta
        this.chunk = carto.chunk
        this.x0 = carto.x0
        this.x1 = carto.x1
        this.y0 = carto.y0
        this.y1 = carto.y1
        doDig()
    }

    abstract fun doDig()

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
    protected fun isWalkableAt(x: Int, y: Int) = chunk.isWalkableAt(x, y)
    protected fun getTerrain(x: Int, y: Int) = chunk.getTerrain(x, y)
    protected fun setTerrain(x: Int, y: Int, type: Terrain.Type) = chunk.setTerrain(x, y, type, Terrain.get(type).isOpaque())
    protected fun safeSetTerrain(x: Int, y: Int, type: Terrain.Type) = carto.safeSetTerrain(x, y, type)
    protected fun spawnThing(x: Int, y: Int, thing: Thing) {
        carto.spawnThing(x, y, thing)
    }
    protected fun biomeAt(x: Int, y: Int) = carto.biomeAt(x, y)

    protected fun flagsAt(x: Int, y: Int) = carto.flagsMap[x - x0][y - y0]

    protected fun carveTrailChunk(room: Rect,
                                  type: Terrain.Type = Terrain.Type.TERRAIN_STONEFLOOR,
                                  skipCorners: Boolean = false, skipTerrain: Terrain.Type? = null) {
        for (x in room.x0..room.x1) {
            for (y in room.y0..room.y1) {
                if (x >= x0 && y >= y0 && x <= x1 && y <= y1) {
                    if (!skipCorners || !((x == x0 || x == x1) && (y == y0 || y == y1))) {
                        if (!flagsAt(x,y).contains(WorldCarto.CellFlag.OCEAN) && !flagsAt(x,y).contains(WorldCarto.CellFlag.BEACH)) {
                            if (skipTerrain == null || getTerrain(x, y) != skipTerrain) {
                                setTerrain(x, y, type)
                                flagsAt(x,y).apply {
                                    add(WorldCarto.CellFlag.TRAIL)
                                    add(WorldCarto.CellFlag.NO_PLANTS)
                                    add(WorldCarto.CellFlag.NO_BUILDINGS)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected fun buildHut(x: Int, y: Int, width: Int, height: Int, fertility: Float) {
        val villagePlantSpawns = villagePlantSpawns()
        val wallType = meta.biome.villageWallType()
        val floorType = meta.biome.villageFloorType()
        val dirtType =  meta.biome.trailTerrain(x0,y0)
        var doorDir = NORTH
        if (y < 28) doorDir = SOUTH
        if (x < 20) doorDir = EAST
        if (x > 40) doorDir = WEST
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
        var windowBlockerCount = Dice.range(3, 8)
        for (tx in x until x+width) {
            for (ty in y until y+height) {
                if (tx == doorx && ty == doory) {
                    setTerrain(x0+tx, y0+ty, floorType)
                    spawnThing(x0+tx, y0+ty, WoodDoor())
                    chunk.setRoofed(x0 + tx, y0 + ty, Chunk.Roofed.WINDOW)
                } else if (tx == x || tx == x+width-1 || ty == y || ty == y+height-1) {
                    setTerrain(x0 + tx, y0 + ty, Terrain.Type.TEMP3)
                } else if (tx == x+1 || tx == x+width-2 || ty == y+1 || ty == y+height-2) {
                    if (windowBlockerCount < 1 && ((tx > x+1 && tx < x+width-2) || (ty > y+1 && ty < y+height-2))) {
                        setTerrain(x0 + tx, y0 + ty, Terrain.Type.TERRAIN_WINDOWWALL)
                        chunk.setRoofed(x0 + tx, y0 + ty, Chunk.Roofed.WINDOW)
                        windowBlockerCount = Dice.range(4, 12)
                    } else {
                        setTerrain(x0 + tx, y0 + ty, wallType)
                        chunk.setRoofed(x0 + tx, y0 + ty, Chunk.Roofed.INDOOR)
                        windowBlockerCount--
                    }
                } else {
                    setTerrain(x0+tx, y0+ty, floorType)
                    chunk.setRoofed(x0 + tx, y0 + ty, Chunk.Roofed.INDOOR)
                }
            }
        }
        safeSetTerrain(x0 + doorx + doorDir.x, y0 + doory + doorDir.y, dirtType)
        val plantDensity = fertility * 2f
        forEachTerrain(Terrain.Type.TEMP3) { x, y ->
            carto.getPlant(meta.biome, meta.habitat, 0.5f,
                plantDensity, villagePlantSpawns
            )?.also { plant ->
                spawnThing(x, y, plant)
            }
            flagsAt(x,y).add(WorldCarto.CellFlag.NO_PLANTS)
        }
        fuzzTerrain(Terrain.Type.TEMP3, 0.4f, listOf(wallType, Terrain.Type.TERRAIN_WINDOWWALL))
        safeSetTerrain(x0 + doorx + doorDir.x, y0 + doory + doorDir.y, dirtType)
        safeSetTerrain(x0 + doorx + doorDir.x*2, y0 + doory + doorDir.y * 2, dirtType)
        swapTerrain(Terrain.Type.TEMP3, meta.biome.baseTerrain)

        if (Dice.chance(0.5f)) {
            val tablex = Dice.range(x+2,x+width-3)
            val tabley = Dice.range(y+2,y+height-3)
            spawnThing(x0+tablex, y0+tabley, Table())
        }
    }
}
