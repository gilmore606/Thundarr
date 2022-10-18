package world.cartos

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import render.tilesets.Glyph
import things.Thing
import util.Dice
import util.Perlin
import world.terrains.PortalDoor
import world.terrains.Terrain
import kotlin.random.Random

class WorldCarto : Carto() {

    val scale = 0.02
    val fullness = 0.002

    override fun doCarveLevel() {
        forEachCell { x, y ->
            val n = Perlin.noise(x.toDouble() * scale, y.toDouble() * scale, 59.0) +
                    Perlin.noise(x.toDouble() * scale * 0.4, y.toDouble() * scale * 0.4, 114.0) * 0.7
            if (n > fullness * scale + Dice.float(0f,0.06f).toDouble()) {
                carve(x, y, 0, Terrain.Type.TERRAIN_DIRT)
            } else {
                carve(x, y, 0, Terrain.Type.TERRAIN_GRASS)
            }
            val n2 = Perlin.noise(x * 0.02, y * 0.03, 8.12) +
                    Perlin.noise(x * 0.041, y * 0.018, 11.17) * 0.8
            if (n2 > 0.01) {
                carve(x, y, 0, Terrain.Type.TERRAIN_BRICKWALL)
            }
        }

        carvePrefab(getPrefab(), Random.nextInt(x0, x1 - 20), Random.nextInt(y0, y1 - 20))

        assignDoors()

        for (x in 0 until width) {
            for (y in 0 until height) {
                if (isWalkableAt(x + this.x0, y + this.y0)) {
                    val n = Perlin.noise(x * 0.04, y * 0.04, 0.01) +
                            Perlin.noise(x * 0.7, y * 0.4, 1.5) * 0.5
                    if (Dice.chance(n.toFloat() * 2.0f)) {
                        addThingAt(x + this.x0, y + this.y0, Thing(
                            Glyph.TREE,
                            true, false
                        )
                        )
                    }
                }
            }
        }
    }

    private fun assignDoors() {
        forEachCell { x, y ->
            if (getTerrain(x, y) == Terrain.Type.TERRAIN_PORTAL_DOOR) {
                val doorId = Random.nextInt(100000).toString()
                setTerrainData(x, y, Json.encodeToString(PortalDoor.Data(
                    enterMsg = "A rusty metal door with the embossed number '$doorId'.\nOpen it and go inside?",
                    levelId = "building" + Random.nextInt(100000).toString()
                )))
            }
        }
    }
}
