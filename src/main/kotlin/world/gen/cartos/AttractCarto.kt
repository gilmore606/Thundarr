package world.gen.cartos

import actors.Herder
import actors.MuskOx
import actors.Ox
import actors.Wolfman
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import things.*
import util.*
import world.*
import world.level.Level
import world.persist.LevelKeeper
import world.terrains.PortalDoor
import world.terrains.Terrain
import kotlin.Exception
import kotlin.random.Random

class AttractCarto(
    x0: Int,
    y0: Int,
    x1: Int,
    y1: Int,
    chunk: Chunk,
    level: Level,
) : Carto(x0, y0, x1, y1, chunk, level) {

    val scale = 0.02
    val fullness = 0.002

    fun carveWorldChunk(offset: Double = 0.0) {
        forEachCell { x, y ->
            val n = Perlin.noise((x.toDouble() + offset) * scale, y.toDouble() * scale, 59.0) +
                    Perlin.noise((x.toDouble() + offset) * scale * 0.4, y.toDouble() * scale * 0.4, 114.0) * 0.7
            if (n > fullness * scale - Dice.float(0f,0.18f).toDouble()) {
                carve(x, y, 0, Terrain.Type.TERRAIN_DIRT)
            } else {
                carve(x, y, 0, Terrain.Type.TERRAIN_GRASS)
            }
            val n2 = Perlin.noise(x * 0.02, y * 0.03, 8.12) +
                    Perlin.noise(x * 0.041, y * 0.018, 11.17) * 0.8
            if (n2 > 0.01) {
                carve(x, y, 0, Terrain.Type.TERRAIN_CAVEWALL)
            }
        }

        for (x in 0 until width) {
            for (y in 0 until height) {
                if (isWalkableAt(x + this.x0, y + this.y0)) {
                    if (Dice.chance(0.01f)) {
                        KtxAsync.launch {
                            if (Dice.chance(0.8f)) {
                                (if (Dice.chance(0.7f)) Ox() else MuskOx()).spawnAt(level, x + x0, y + y0)
                            } else if (Dice.flip()) {
                                Wolfman().spawnAt(level, x + x0, y + y0)
                            } else {
                                Herder().spawnAt(level, x + x0, y + y0)
                            }
                        }
                    }
                    val n = Perlin.noise(x * 0.04, y * 0.04, 0.01) +
                            Perlin.noise(x * 0.7, y * 0.4, 1.5) * 0.5
                    if (Dice.chance(n.toFloat() * 1.6f)) {
                        addThing(x + this.x0, y + this.y0, if (Dice.flip()) OakTree() else PineTree())
                        if (Dice.chance(0.2f)) {
                            var clear = true
                            CARDINALS.forEach { dir ->
                                if (chunk.thingsAt(x + dir.x + this.x0,y + dir.y + this.y0).size > 0) {
                                    clear = false
                                }
                            }
                            if (clear) {
                                val dir = CARDINALS.random()
                                try {

                                    addThing(x + this.x0 + dir.x, y + this.y0 + dir.y, when (Random.nextInt(4)) {
                                        0 -> Apple()
                                        1 -> Axe()
                                        2 -> Pear()
                                        3 -> Pickaxe()
                                        else -> EnergyDrink()
                                    })
                                } catch (_: Exception) { }
                            }
                        }
                    }
                }
            }
        }

        setRoofedInRock()
        setOverlaps()
    }

}
