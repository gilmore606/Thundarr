package world.gen.cartos

import App
import actors.NPC
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import things.NPCDen
import util.*
import world.*
import world.gen.AnimalSpawn
import world.gen.AnimalSpawnSource
import world.gen.animalSpawns
import world.gen.biomes.Biome
import world.gen.biomes.Ocean
import world.gen.features.Feature
import world.level.CHUNK_SIZE
import world.level.Level
import world.terrains.Terrain
import world.terrains.Terrain.Type.*
import java.lang.Float.max
import java.lang.Float.min
import kotlin.random.Random

class WorldCarto(
    x0: Int,
    y0: Int,
    x1: Int,
    y1: Int,
    chunk: Chunk,
    level: Level,
    val forStarter: Boolean = false
) : Carto(x0, y0, x1, y1, chunk, level) {

    val chunkBlendWidth = 6
    val chunkBlendCornerRadius = 4

    val globalPlantDensity = 0.2f

    enum class CellFlag { NO_PLANTS, NO_BUILDINGS, BLOCK_TRAILS, BRIDGE_SLOT, RIVER, RIVERBANK, OCEAN, BEACH }

    private val neighborMetas = mutableMapOf<XY,ChunkMeta?>()
    private val blendMap = Array(CHUNK_SIZE) { Array(CHUNK_SIZE) { mutableSetOf<Pair<Biome, Float>>() } }
    val flagsMap = Array(CHUNK_SIZE) { Array(CHUNK_SIZE) { mutableSetOf<CellFlag>() } }
    private val fertMap = Array(CHUNK_SIZE) { Array<Float?>(CHUNK_SIZE) { null } }

    private var hasBlends = false
    lateinit var meta: ChunkMeta

    suspend fun carveWorldChunk() {
        meta = App.save.getWorldMeta(x0, y0) ?: throw RuntimeException("No meta found for chunk $x0 $y0 !")

        if (meta.biome == Ocean) {
            carveRoom(Rect(x0,y0,x1,y1), 0, TERRAIN_DEEP_WATER)
        } else {

            buildBiomeBlendMap()
            carveBlendedTerrain()
            digFeatures(Feature.Stage.TERRAIN)

            meta.biome.carveExtraTerrain(this)
            digFeatures(Feature.Stage.BUILD)
            if (Dice.chance(0.01f) || forStarter) buildStructureDungeon()

            // Plants and stuff
            buildFertilityMap()
            growPlants()
            meta.biome.placeExtraThings(this)

            // Post-processing
            deepenWater()
            pruneTrees()
            setRoofedInRock()
            meta.biome.postProcess(this)
            setOverlaps()

            // Creatures
            spawnAnimals()
        }

        //debugBorders()
    }

    private fun digFeatures(stage: Feature.Stage) {
        meta.features().filter { it.stage() == stage }.sortedBy { it.order() }.forEach { feature ->
            feature.dig(this)
        }
    }

    fun setFlag(x: Int, y: Int, flag: CellFlag) {
        flagsMap[x-x0][y-y0].add(flag)
    }

    private fun pruneTrees() {
        forEachCell { x,y ->
            if (y in (y0 + 1) until y1) {
                val type = getTerrain(x, y)
                if (Terrain.get(type).pruneVerticalOrphans()) {
                    val upper = getTerrain(x, y - 1)
                    val lower = getTerrain(x, y + 1)
                    if (upper != type && lower != type) {
                        setTerrain(x, y, if (Dice.flip()) upper else lower)
                    }
                }
            }
        }
    }

    private fun buildFertilityMap() {
        forEachBiome { x,y,biome ->
            val terrain = Terrain.get(getTerrain(x,y))
            if (!flagsMap[x-x0][y-y0].contains(CellFlag.NO_PLANTS) && terrain.canGrowPlants) {
                var fert = biome.fertilityAt(x, y) + terrain.fertilityBonus()
                val nearTrees = neighborCount(x, y, DIRECTIONS) { x,y -> getTerrain(x,y) == TERRAIN_TEMPERATE_FORESTWALL } > 0
                val nearWater = neighborCount(x, y, DIRECTIONS) { x,y -> getTerrain(x,y) == GENERIC_WATER } > 0
                if (nearTrees) fert += 0.4f
                if (nearWater) fert += 0.4f
                if (Dice.chance(0.2f)) fert += Dice.float(-0.3f, 0.3f)
                fertMap[x-x0][y-y0] = max(0.0f, min(1.0f, fert))
            }
        }
    }

    private fun growPlants() {
        forEachBiome { x,y,biome ->
            fertMap[x-x0][y-y0]?.also { fertility ->
                getPlant(biome, meta.habitat, fertility, globalPlantDensity)?.also {
                    spawnThing(x, y, it)
                }
            }
        }
    }

    private fun spawnAnimals() {
        var groupsSpawned = 0
        // Spawn for chunk features
        meta.features().forEach { feature ->
            groupsSpawned = spawnAnimalsFromGroups(groupsSpawned, feature.animalSpawns(), feature)
        }
        // Spawn for general biome/habitat
        groupsSpawned = spawnAnimalsFromGroups(groupsSpawned, animalSpawns())
    }

    private fun spawnAnimalsFromGroups(groupsSpawned: Int, groupsPool: List<AnimalSpawn>, source: AnimalSpawnSource? = null): Int {
        val maxGroups = 2
        var spawned = groupsSpawned
        val pool = groupsPool.filter { it.biomes.contains(meta.biome) && it.habitats.contains(meta.habitat) }.shuffled()
        pool.forEach { group ->
            if (spawned < maxGroups && Dice.chance(group.frequency)) {
                repeat(Dice.range(group.min, group.max)) {
                    val animalType = group.tag.invoke()
                    (source ?: meta.biome).animalSpawnPoint(chunk, animalType)?.also { location ->
                        spawnThing(location.x, location.y, NPCDen(animalType))
                    }
                }
                spawned++
            }
        }
        return spawned
    }

    private suspend fun buildBiomeBlendMap() {
        neighborMetas.apply {
            DIRECTIONS.forEach { dir ->
                val neighbor = App.save.getWorldMeta(x0 + dir.x * CHUNK_SIZE, y0 + dir.y * CHUNK_SIZE)
                if (neighbor == null || neighbor.biome == Ocean || neighbor.biome == meta.biome) {
                    set(dir, null)
                } else {
                    set(dir, neighbor)
                    hasBlends = true
                }
            }
        }
        if (hasBlends) {
            blendMap.forEach { it.forEach { it.add(Pair(meta.biome, 1f)) }}
            // Blend sides
            for (i in 0 until CHUNK_SIZE) {
                for (j in 0 until chunkBlendWidth) {
                    val neighborWeight = 1f - (j * (1f / chunkBlendWidth))
                    neighborMetas[NORTH]?.also { blendMap[i][j].add(Pair(it.biome, neighborWeight)) }
                    neighborMetas[SOUTH]?.also { blendMap[i][CHUNK_SIZE-j-1].add(Pair(it.biome, neighborWeight)) }
                    neighborMetas[WEST]?.also { blendMap[j][i].add(Pair(it.biome, neighborWeight)) }
                    neighborMetas[EAST]?.also { blendMap[CHUNK_SIZE-j-1][i].add(Pair(it.biome, neighborWeight)) }
                }
            }
            // Blend corners
            val maxWidth = chunkBlendWidth + chunkBlendCornerRadius
            for (i in 0 until maxWidth) {
                for (j in 0 until maxWidth) {
                    val neighborWeight = 1f - (i+j).toFloat()/(maxWidth*2).toFloat()
                    val outsideCornerWeight = kotlin.math.max(0f, neighborWeight - 0.5f)
                    neighborMetas[NORTHWEST]?.also { blendMap[i][j].addIfHeavier(it.biome,
                        if (neighborMetas[NORTH]?.biome == it.biome || neighborMetas[WEST]?.biome == it.biome) neighborWeight else outsideCornerWeight) }
                    neighborMetas[NORTHEAST]?.also { blendMap[CHUNK_SIZE-i-1][j].addIfHeavier(it.biome,
                        if (neighborMetas[NORTH]?.biome == it.biome || neighborMetas[EAST]?.biome == it.biome) neighborWeight else outsideCornerWeight) }
                    neighborMetas[SOUTHWEST]?.also { blendMap[i][CHUNK_SIZE-j-1].addIfHeavier(it.biome,
                        if (neighborMetas[SOUTH]?.biome == it.biome || neighborMetas[WEST]?.biome == it.biome) neighborWeight else outsideCornerWeight) }
                    neighborMetas[SOUTHEAST]?.also { blendMap[CHUNK_SIZE-i-1][CHUNK_SIZE-j-1].addIfHeavier(it.biome,
                        if (neighborMetas[SOUTH]?.biome == it.biome || neighborMetas[EAST]?.biome == it.biome) neighborWeight else outsideCornerWeight) }
                }
            }
        }
        // Roll each cell and set a specific biome by removing all other biome pairs from the blendmap
        forEachCell { x,y ->
            var biome: Biome? = null
            if (hasBlends) {
                val weights = blendMap[x - x0][y - y0]
                var weightTotal = 0f
                weights.forEach { weightTotal += it.second }
                var roll = Dice.float(0f, weightTotal)
                weights.forEach {
                    if (biome == null && roll <= it.second) {
                        biome = it.first
                    } else {
                        roll -= it.second
                    }
                }
            }
            blendMap[x - x0][y - y0].clear()
            blendMap[x - x0][y - y0].add(Pair(biome ?: meta.biome, 1f))
        }
        // Fill in orphaned single-cells in the blend with a neighbor
        forEachBiome { x, y, biome ->
            var switch: Biome? = null
            var orphan = true
            CARDINALS.from(x, y) { tx, ty, _ ->
                if (boundsCheck(tx, ty)) {
                    if (blendMap[tx-x0][ty-y0].first().first == biome) orphan = false
                    else switch = blendMap[tx-x0][ty-y0].first().first // TODO: make this random!
                }
            }
            if (orphan) blendMap[x-x0][y-y0].apply {
                clear()
                add(Pair(switch!!, 1f))
            }
        }
    }

    fun forEachBiome(doThis: (x: Int, y: Int, biome: Biome)->Unit) {
        forEachCell { x,y ->
            var biome = meta.biome
            blendMap[x-x0][y-y0].also { if (it.isNotEmpty()) biome = it.first().first }
            doThis(x, y, biome)
        }
    }

    fun biomeAt(x: Int, y: Int) = if (boundsCheck(x, y)) {
        blendMap[x-x0][y-y0].let { if (it.isNotEmpty()) it.first().first else meta.biome }
    } else meta.biome

    private fun carveBlendedTerrain() {
        forEachBiome { x,y,biome ->
            var t = biome.terrainAt(x,y)
            if (t == TERRAIN_TEMPERATE_FORESTWALL) t = meta.habitat.forestWallType()
            carve(x, y, 0, t)
        }
        // Biome-specific post-blend processing
        if (hasBlends) {
            neighborMetas.forEach { dir, neighborMeta ->
                val bounds = when (dir) {
                    NORTH -> Rect(0,0,CHUNK_SIZE-1,chunkBlendWidth-1)
                    NORTHEAST -> Rect(CHUNK_SIZE-1-(chunkBlendWidth + chunkBlendCornerRadius), 0, CHUNK_SIZE-1,chunkBlendWidth-1)
                    EAST -> Rect(CHUNK_SIZE-1-chunkBlendWidth,0, CHUNK_SIZE-1, CHUNK_SIZE-1)
                    SOUTHEAST -> Rect(CHUNK_SIZE-1-(chunkBlendWidth + chunkBlendCornerRadius), CHUNK_SIZE-1-chunkBlendWidth, CHUNK_SIZE-1, CHUNK_SIZE-1)
                    SOUTH -> Rect(0, CHUNK_SIZE-1-chunkBlendWidth, CHUNK_SIZE-1, CHUNK_SIZE-1)
                    SOUTHWEST -> Rect(0,CHUNK_SIZE-1-chunkBlendWidth,chunkBlendWidth-1, CHUNK_SIZE-1)
                    WEST -> Rect(0,0,chunkBlendWidth-1, CHUNK_SIZE-1)
                    else -> Rect(0,0,chunkBlendWidth-1,chunkBlendWidth-1)
                }
                neighborMeta?.biome?.postBlendProcess(this, bounds)
            }
        }
    }

    private fun buildStructureDungeon() {
        val facing = CARDINALS.random()
        carvePrefab(getPrefab(), Random.nextInt(x0, x1 - 20), Random.nextInt(y0, y1 - 20), facing)
        if (forStarter) {
            log.info("Looking for door for starter dungeon...")
        } else {
            log.info("Building structure dungeon...")
        }
        forEachCell { x, y ->
            if (getTerrain(x, y) == TERRAIN_PORTAL_DOOR) {
                val building = if (forStarter)
                    StarterDungeon().at(x,y).facing(facing)
                else
                    BoringBuilding().at(x,y).facing(facing)
                connectBuilding(building)
            }
        }
    }

    fun addTrailBlock(x0: Int, y0: Int, x1: Int, y1: Int) {
        for (dx in x0-1..x1+1) {
            for (dy in y0-1..y1+1) {
                if (dx in this.x0..this.x1 && dy in this.y0..this.y1) {
                    setFlag(dx, dy, CellFlag.BLOCK_TRAILS)
                }
            }
        }
    }

    fun blockTrailAt(x0: Int, y0: Int) {
        for (dx in x0-1..x0+1) {
            for (dy in y0-1..y0+1) {
                if (dx in x0..x1 && dy in y0..y1)
                    setFlag(dx, dy, CellFlag.BLOCK_TRAILS)
            }
        }
    }

    override fun ignoreCellForWaterDeepening(x: Int, y: Int) = flagsMap[x-x0][y-y0].contains(CellFlag.BRIDGE_SLOT)
}
