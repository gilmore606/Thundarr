package world.gen.features

import kotlinx.serialization.Serializable
import things.HighwaySign
import things.WreckedCar
import util.*
import world.gen.NoisePatches
import world.gen.cartos.WorldCarto
import world.level.CHUNK_SIZE
import world.terrains.Highway
import world.terrains.Terrain

@Serializable
class Highways(
    val exits: MutableList<HighwayExit>,
) : ChunkFeature(
    5, Stage.TERRAIN
) {

    private val carChance = 0.4f
    private val signOffset = Dice.range(3, 10)
    private val signSide = if (Dice.flip()) 2 else -2

    @Serializable
    class HighwayExit(
        var edge: XY,
        var width: Int = 2,
        var sign: String? = null
    )

    fun addExit(exit: HighwayExit) {
        exits.add(exit)
    }

    override fun doDig() {
        val mid = CHUNK_SIZE /2
        val end = CHUNK_SIZE -1
        exits.forEach { exit ->
            when (exit.edge) {
                NORTH -> drawLine(XY(mid, 0), XY(mid, mid)) { x,y -> buildRoadCell(x,y,exit,true) }
                SOUTH -> drawLine(XY(mid, mid), XY(mid,end)) { x,y -> buildRoadCell(x,y,exit,true) }
                WEST -> drawLine(XY(0,mid+1), XY(mid, mid+1)) { x,y -> buildRoadCell(x,y,exit,false) }
                EAST -> drawLine(XY(mid, mid+1), XY(end, mid+1)) { x,y -> buildRoadCell(x,y,exit,false) }
            }
        }
        if (Dice.chance(carChance)) {
            repeat (Dice.oneTo(3)) {
                var placed = false
                while (!placed) {
                    val tx = Dice.range(x0, x1)
                    val ty = Dice.range(y0, y1)
                    if (Terrain.get(getTerrain(tx, ty)) is Highway) {
                        spawnThing(tx, ty, WreckedCar())
                        placed = true
                    }
                }
            }
        }
    }

    private fun buildRoadCell(x: Int, y: Int, exit: HighwayExit, isVertical: Boolean) {
        repeat (exit.width + 2) { n ->
            val lx = if (isVertical) x + n + x0 - (exit.width / 2) else x + x0
            val ly = if (isVertical) y + y0 else y + n + y0 - (exit.width / 2)
            val wear = NoisePatches.get("ruinWear", lx, ly).toFloat()
            val current = getTerrain(lx, ly)
            flagsAt(lx,ly).add(WorldCarto.CellFlag.NO_BUILDINGS)
            if (n == 0 || n == exit.width + 1) {
                if (current != Terrain.Type.TERRAIN_HIGHWAY_H && current != Terrain.Type.TERRAIN_HIGHWAY_V && current != Terrain.Type.GENERIC_WATER) {
                    if (wear < 0.001f || Dice.chance(1f - wear)) {
                        setTerrain(lx, ly, biomeAt(lx, ly).trailTerrain(lx, ly))
                    }
                }
            } else {
                val t = if (isVertical) Terrain.Type.TERRAIN_HIGHWAY_V else Terrain.Type.TERRAIN_HIGHWAY_H
                if (wear < 0.34f || (current != Terrain.Type.GENERIC_WATER && Dice.chance(0.7f - wear))) {
                    setTerrain(lx, ly, t)
                } else if (current != Terrain.Type.GENERIC_WATER) {
                    setTerrain(lx, ly, biomeAt(lx, ly).trailTerrain(lx, ly))
                    flagsAt(lx,ly).add(WorldCarto.CellFlag.NO_PLANTS)
                }
            }
        }
        exit.sign?.also { signText ->
            if (isVertical && y == signOffset) {
                spawnThing(x0 + x + signSide, y0 + y, HighwaySign(signText))
            } else if (!isVertical && x == signOffset) {
                spawnThing(x0 + x, y0 + y + signSide, HighwaySign(signText))
            }
        }
    }
}
