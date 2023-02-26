package world.gen.features

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import util.*
import world.ChunkScratch
import world.gen.biomes.Ocean
import world.gen.biomes.Ruins
import world.gen.biomes.Suburb
import world.gen.cartos.WorldCarto
import world.path.DistanceMap
import world.terrains.Terrain
import java.time.Year

@Serializable
class Trails(
    val exits: MutableList<TrailExit>
) : ChunkFeature(
    5, Stage.BUILD
) {
    companion object {
        fun canBuildOn(meta: ChunkScratch) = meta.biome !in listOf(Ruins, Suburb, Ocean)
    }

    @Serializable
    class TrailExit(
        var pos: XY,
        var edge: XY,
    )

    fun addExit(exit: TrailExit) {
        exits.add(exit)
    }

    @Transient lateinit var walkMap: DistanceMap

    private fun openAt(x: Int, y: Int) = boundsCheck(x, y) && flagsAt(x,y).contains(WorldCarto.CellFlag.BLOCK_TRAILS)

    override fun doDig() {
        // pick unblocked point near center as Target
        var tries = 0
        var found = false
        var center: XY? = null
        while (tries < 200 && !found) {
            tries++
            val centerX = 22 + Dice.zeroTil(20)
            val centerY = 22 + Dice.zeroTil(20)
            if (openAt(x0+centerX, y0+centerY)) {
                found = true
                center = XY(x0+centerX, y0+centerY)
            }
        }
        if (!found) return   // TODO: deal with this case, look further from center

        // build DistanceMap on trailBlockmap to Target
        walkMap = DistanceMap(chunk, { x, y ->
            x == center!!.x && y == center!!.y
        }, { x,y ->
            openAt(x,y)
        })

        // walk from each trail exit to Target
        exits.forEach { exit ->
            trailToCenter(exit.pos)
        }
        // swap in terrain and add some edges
        forEachCell { x,y ->
            if (getTerrain(x, y) == Terrain.Type.TEMP4) {
                setTerrain(x,y,Terrain.Type.TERRAIN_TRAIL)
                CARDINALS.from(x,y) { dx,dy,dir ->
                    if (boundsCheck(dx,dy)) {
                        val t = getTerrain(dx, dy)
                        if (Terrain.get(t).trailsOverwrite() && t !in listOf(
                                Terrain.Type.TEMP4,
                                Terrain.Type.TERRAIN_TRAIL
                            )
                        ) {
                            if (Dice.chance(meta.variance * 0.5f)) {
                                setTerrain(dx, dy, meta.biome.trailSideTerrain(dx, dy))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun trailToCenter(source: XY) {
        var cursor = XY(source.x + x0, source.y + y0)
        var done = false
        while (!done) {
            drawTrailCell(cursor.x, cursor.y)
            val step = walkMap.distanceAt(cursor.x, cursor.y)
            if (step < 1) done = true
            else {
                val poss = mutableListOf<XY>()
                for (dx in -1..1) {
                    for (dy in -1..1) {
                        val next = walkMap.distanceAt(cursor.x + dx, cursor.y + dy)
                        if (next == step - 1) {
                            poss.add(XY(cursor.x + dx, cursor.y + dy))
                        }
                    }
                }
                if (poss.isEmpty()) done = true
                else {
                    cursor = poss.random()
                }
            }
        }
    }

    private fun drawTrailCell(x: Int, y: Int) {
        for (tx in 0 .. 1) {
            for (ty in 0..1) {
                val dx = tx + x
                val dy = ty + y
                if (boundsCheck(dx, dy) && Dice.chance(0.9f)) {
                    val t = getTerrain(dx,dy)
                    if (Terrain.get(t).trailsOverwrite()) {
                        setTerrain(dx, dy, Terrain.Type.TEMP4)
                    }
                }
            }
        }
    }
}
