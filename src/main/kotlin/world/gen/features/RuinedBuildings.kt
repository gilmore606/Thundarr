package world.gen.features

import actors.actors.NPC
import kotlinx.serialization.Serializable
import things.Bonepile
import things.Chest
import things.ModernDoor
import things.Trunk
import util.*
import world.Chunk
import world.ChunkScratch
import world.gen.NoisePatches
import world.gen.biomes.Ocean
import world.gen.cartos.WorldCarto
import world.gen.habitats.Habitat
import world.gen.spawnsets.AnimalSet
import world.gen.spawnsets.RuinLoot
import world.level.CHUNK_SIZE
import world.quests.FetchQuest
import world.terrains.Terrain

@Serializable
class RuinedBuildings(
    val buildingCount: Int,
) : Feature() {
    override fun order() = 1
    override fun stage() = Stage.BUILD
    override fun canBeQuestDestination() = Dice.chance(0.1f)
    override fun createQuest() = FetchQuest()
    override fun name() = "ruined building"

    companion object {
        fun canBuildOn(meta: ChunkScratch) = !meta.hasFeature(Village::class) && meta.biome !in listOf(Ocean)

        val animalSpawns = AnimalSet().apply {
            add(1f, NPC.Tag.WOLFMAN)
        }
    }

    private val ruinIntactChance = 0.25f

    private var alreadyMadeResident = false
    private var residentSpawnRect: Rect? = null

    override fun trailDestinationChance() = 0.6f

    override fun animalSpawnCount() = when (Dice.oneTo(10)) {
        1 -> 2
        2 -> 0
        else -> 1
    }

    override fun animalSet(habitat: Habitat) = animalSpawns
    override fun animalSpawnPoint(chunk: Chunk, animal: NPC, near: XY?, within: Float?): XY? = residentSpawnRect?.randomPoint()

    override fun doDig() {
        repeat (buildingCount) {
            buildRandomRuin()
        }
    }

    private fun buildRandomRuin() {
        val isIntact = Dice.chance(ruinIntactChance)
        val mid = CHUNK_SIZE /2
        if (meta.hasFeature(Highways::class)) {
            val offset = Dice.range(3, 8) * Dice.sign()
            val along = Dice.range(2, CHUNK_SIZE /2-2)
            val width = Dice.range(4, 10)
            val height = Dice.range(4, 10)
            when (meta.highways().random().edge) {
                NORTH -> buildRuin(mid + offset + width * (if (offset<0) -1 else 0), along, width, height, isIntact)
                SOUTH -> buildRuin(mid + offset + width * (if (offset<0) -1 else 0), CHUNK_SIZE -along, width, height, isIntact)
                WEST -> buildRuin(along, mid + offset + width * (if (offset<0) -1 else 0), width, height, isIntact)
                EAST -> buildRuin(CHUNK_SIZE -along, mid + offset + width * (if (offset<0) -1 else 0), width, height, isIntact)
            }
        } else {
            buildRuin(Dice.range(1, CHUNK_SIZE -10), Dice.range(1, CHUNK_SIZE -10), Dice.range(4, 10), Dice.range(4, 10), isIntact)
        }
    }

    private fun buildRuin(x: Int, y: Int, width: Int, height: Int, isIntact: Boolean = false) {
        val wallChance = if (isIntact) 0.96f else Dice.float(0.7f, 0.9f)
        val floorChance = if (isIntact) 2f else 1f
        var blocked = false
        forXY(x,y, x+width-1,y+height-1) { ix,iy ->
            if (ix>=0 && iy>=0 && ix<CHUNK_SIZE && iy<CHUNK_SIZE
                && carto.flagsMap[ix][iy].contains(WorldCarto.CellFlag.NO_BUILDINGS)) blocked = true
        }
        if (blocked) return
        carto.addTrailBlock(x0+x, y0+y, x0+x+width-1, y0+y+height-1)
        forXY(x,y, x+width-1,y+height-1) { ix,iy ->
            if (boundsCheck(ix + x0, iy + y0)) {
                if (ix == x || ix == x + width - 1 || iy == y || iy == y + height - 1) {
                    val terrain = if (Dice.chance(wallChance)) Terrain.Type.TERRAIN_BRICKWALL else null
                    setRuinTerrain(ix + x0, iy + y0, 0.34f, terrain)
                } else {
                    val terrain =
                        if (Dice.chance(NoisePatches.get("ruinWear", ix + x0, iy + y0).toFloat() * floorChance))
                            null else Terrain.Type.TERRAIN_STONEFLOOR
                    setRuinTerrain(ix + x0, iy + y0, 0.34f, terrain)
                }
                if (isIntact) chunk.setRoofed(ix + x0, iy + y0, Chunk.Roofed.INDOOR)
            }
        }
        val doorDir = CARDINALS.random()
        val doorx = if (doorDir == NORTH || doorDir == SOUTH) {
            Dice.range(x+1, x+width-2)
        } else {
            if (doorDir == EAST) x+width-1 else x
        } + x0
        val doory = if (doorDir == EAST || doorDir == WEST) {
            Dice.range(y+1, y+height-2)
        } else {
            if (doorDir == SOUTH) y+height-1 else y
        } + y0
        if (boundsCheck(doorx, doory)) {
            setTerrain(doorx, doory, Terrain.Type.TERRAIN_STONEFLOOR)
            if (isIntact || Dice.chance(0.2f)) {
                if (isIntact) chunk.setRoofed(doorx, doory, Chunk.Roofed.WINDOW)
                spawnThing(doorx, doory, ModernDoor())
            }
        }

        // Place treasure chest
        if (!alreadyMadeResident) {
            findWalkablePoint(x0 + x - 2, y0 + y - 2, x0 + x + width + 2, y0 + y + height + 2)?.also { xy ->
                val treasure = when (Dice.oneTo(3)) {
                    1 -> Trunk()
                    2 -> Chest()
                    else -> Bonepile()
                }.withLoot(RuinLoot.set, Dice.range(2, 6), carto.threatLevel + 1)
                spawnThing(xy.x, xy.y, treasure)
            }
            residentSpawnRect = Rect(x0 + x + 1, y0 + y + 1, x0 + x + width - 2, y0 + y + height - 2)
            alreadyMadeResident = true
        }

        // Place bonepiles
        repeat (Dice.zeroTo(2)) {
            findWalkablePoint(x0 + x - 4, y0 + y - 4, x0 + x + width + 4, y0 + y + height + 4) { xy ->
                !isRoofedAt(xy.x, xy.y)
            }?.also { xy ->
                Bonepile().withDefaultLoot(carto.threatLevel + 1).apply {
                    spawnThing(xy.x, xy.y, this)
                }
            }
        }

    }

    private fun setRuinTerrain(x: Int, y: Int, wear: Float, terrain: Terrain.Type?) {
        if (terrain == null) return
        if (NoisePatches.get("ruinWear", x, y) < wear) {
            if (boundsCheck(x, y)) {
                setTerrain(x, y, terrain)
            }
        }
    }
}
