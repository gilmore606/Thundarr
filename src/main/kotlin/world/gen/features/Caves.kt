package world.gen.features

import actors.NPC
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Glowstone
import util.*
import world.Chunk
import world.ChunkScratch
import world.NaturalCavern
import world.gen.AnimalSpawn
import world.gen.Metamap
import world.gen.biomes.Desert
import world.gen.biomes.ForestHill
import world.gen.biomes.Hill
import world.gen.biomes.Mountain
import world.gen.habitats.*
import world.quests.FetchQuest
import world.terrains.Terrain

@Serializable
class Caves : Feature() {
    override fun order() = 4
    override fun stage() = Stage.BUILD
    override fun canBeQuestDestination() = Dice.chance(0.5f)
    override fun createQuest() = FetchQuest()
    override fun name() = "caves"

    companion object {
        fun canBuildOn(meta: ChunkScratch) = !meta.hasFeature(Village::class)
                && meta.biome in listOf(Mountain, Hill, ForestHill, Desert)
    }

    private val cavePortalChance = 0.15f
    private val cavePoints = ArrayList<XY>()

    var hasCavern = false

    override fun trailDestinationChance() = 0.4f

    override fun loreKnowabilityRadius() = 250
    override fun loreName() = "caverns"
    override fun xpValue() = 3

    override fun doDig() {
        val entrances = mutableSetOf<XY>()
        forEachTerrain(Terrain.Type.TERRAIN_CAVEWALL) { x, y ->
            val free = neighborCount(x, y, CARDINALS) { dx, dy -> isWalkableAt(dx,dy) }
            if (free == 1) entrances.add(XY(x,y))
        }
        if (entrances.isNotEmpty()) {
            cavePoints.clear()
            var cellCount = 0
            repeat (kotlin.math.min(entrances.size, Dice.oneTo(4))) {
                val entrance = entrances.random()
                cellCount += recurseCave(entrance.x, entrance.y, 1f, Dice.float(0.02f, 0.12f))
                chunk.setRoofed(entrance.x, entrance.y, Chunk.Roofed.WINDOW)
            }
            if (cellCount > 6) {
                val usablePoints = cavePoints.filter { point ->
                    CARDINALS.hasOneWhere { !isWalkableAt(it.x + point.x, it.y + point.y) }
                }.toMutableList()
                if (usablePoints.isNotEmpty() && Dice.chance(cavePortalChance)) {
                    val caveEntrance = usablePoints.random()
                    usablePoints.remove(caveEntrance)
                    buildCaveDungeon(caveEntrance)
                    if (usablePoints.isNotEmpty()) {
                        val lightPos = usablePoints.random()
                        val light = Glowstone().withColor(0.1f, 0.2f, 0.5f)
                        spawnThing(lightPos.x, lightPos.y, light)
                    }
                }
            }
        }
    }

    private fun recurseCave(x: Int, y: Int, density: Float, falloff: Float): Int {
        setTerrain(x, y, Terrain.Type.TERRAIN_CAVEFLOOR)
        chunk.setRoofed(x, y, Chunk.Roofed.INDOOR)
        carto.blockTrailAt(x, y)
        var continuing = false
        var count = 1
        CARDINALS.from(x, y) { dx, dy, _ ->
            if (boundsCheck(dx, dy) && getTerrain(dx, dy) == Terrain.Type.TERRAIN_CAVEWALL) {
                var ok = true
                DIRECTIONS.from(dx, dy) { ddx, ddy, _ ->
                    if (boundsCheck(ddx,ddy)) {
                        val testTerrain = getTerrain(ddx,ddy)
                        if (testTerrain != Terrain.Type.TERRAIN_CAVEFLOOR && testTerrain != Terrain.Type.TERRAIN_CAVEWALL) ok = false
                    }
                }
                if (ok && Dice.chance(density)) {
                    continuing = true
                    count += recurseCave(dx, dy, density - falloff, falloff)
                }
            }
        }
        if (!continuing) cavePoints.add(XY(x, y))
        return count
    }

    private fun buildCaveDungeon(doorPos: XY) {
        log.info("Building cave dungeon...")
        setTerrain(doorPos.x, doorPos.y, Terrain.Type.TERRAIN_PORTAL_CAVE)
        val facings = mutableListOf<XY>()
        CARDINALS.from(doorPos.x, doorPos.y) { dx, dy, dir ->
            if (boundsCheck(dx,dy) && isWalkableAt(dx,dy)) facings.add(dir)
        }
        carto.connectBuilding(NaturalCavern().at(doorPos.x, doorPos.y).facing(facings.random()))
        hasCavern = true
        Metamap.update(meta)
    }

    override fun mapIcon() = if (hasCavern) Glyph.MAP_CAVE else null
    override fun mapPOITitle() = if (hasCavern) "cavern" else null
    override fun mapPOIDescription() = if (hasCavern) "An underground cave system." else null

    override fun animalSpawns() = listOf(
        AnimalSpawn(
            { NPC.Tag.NPC_GRIZZLER },
            setOf(Mountain, Hill, ForestHill, Desert),
            setOf(TemperateA, TemperateB, TropicalA, TropicalB, AlpineA, AlpineB),
            0f, 1000f, 1, 1, 1f
        )
    )

    override fun animalSpawnPoint(chunk: Chunk, animalType: NPC.Tag): XY? = cavePoints.randomOrNull()
}
