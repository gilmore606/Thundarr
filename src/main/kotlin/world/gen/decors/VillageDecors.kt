package world.gen.decors

import kotlinx.serialization.Serializable
import things.*
import util.Dice
import world.gen.biomes.Biome
import world.gen.cartos.WorldCarto
import world.gen.habitats.Habitat
import world.gen.gardenPlantSpawns
import world.terrains.Terrain

@Serializable
class Hut : Decor() {
    override fun description() = listOf(
        "A simple village hut.",
        "A single-room country dwelling.",
    ).random()
    override fun doFurnish() {
        againstWall { spawn(Bedroll()) }
        chance(0.3f) { againstWall { spawn(Trunk()) } }
        chance(0.6f) { awayFromWall { spawn(Table()) } }
    }
}

@Serializable
class HutBedroom : Decor() {
    override fun description() = listOf(
        "A spartan but cozy bedroom.",
        "A somewhat dishevelled villager's bedroom.",
        "A simple bedchamber.",
    ).random()
    override fun doFurnish() {
        againstWall { spawn(Bedroll()) }
        chance(0.4f) { againstWall { spawn(Trunk()) } }
    }
}

@Serializable
class HutLivingRoom : Decor() {
    override fun description() = "A simple villager's living room."
    override fun doFurnish() {
        chance(0.7f) { anyEmpty { spawn(Table()) } }
        if (clearCount > 15) {
            anyEmpty { spawn(Table()) }
        }
        if (clearCount > 20) {
            chance(0.6f) { againstWall { spawn(
                if (Dice.chance(0.7f)) FilingCabinet() else Bookshelf()
            )}}
        }
    }
}

@Serializable
class Schoolhouse : Decor() {
    override fun description() = "A country schoolhouse lined with small desks."
    override fun abandonedDescription() = "An abandoned country schoolhouse, the small desks covered in dust and cobwebs."
    override fun fitsInRoom(room: Room) = room.width >= 5 && room.height >= 5

    override fun doFurnish() {
        repeat(Dice.oneTo(2)) { againstWall {
            spawn(Bookshelf())
            clearAround()
        } }
        forEachClear { x,y ->
            if (x % 2 == 0 && y % 2 == 0) spawn(Table())
        }
    }
}

@Serializable
class Church : Decor() {
    override fun description() = "A humble but lovingly constructed shrine to the Lords of Light."
    override fun abandonedDescription() = "An abandoned shrine to the Lords of Light, now covered in dust."
    override fun doFurnish() {
        atCenter { spawn(
            if (!isAbandoned || Dice.chance(0.5f)) Shrine() else Boulder()
        ) }
        repeat(Dice.zeroTo(2)) { againstWall { spawn(Table()) }}
    }
}

@Serializable
class StorageShed : Decor() {
    override fun description() = "A musty-smelling storage shed."
    override fun fitsInRoom(room: Room) = room.width <= 6 && room.height <= 6

    override fun doFurnish() {
        repeat (Dice.range(4, 8)) {
            awayFromWall { spawn(if (Dice.flip()) Trunk() else FilingCabinet()) }
        }
        if (Dice.flip()) repeat (Dice.range(1, 3)) {
            againstWall { spawn(Table()) }
        }
    }
}

@Serializable
class BlacksmithShop : Decor() {
    override fun description() = "A blacksmith's shop."
    override fun abandonedDescription() = "An abandoned blacksmith's shop."
    override fun doFurnish() {
        forArea(x0+1, y0+1, x1-1, y1-1) { x,y ->
            setTerrain(x, y, Terrain.Type.TERRAIN_DIRT, roofed = true)
        }
        atCenter { spawn(Forge()) }
        repeat (Dice.range(1, 4)) {
            againstWall { spawn(Table()) }
        }
    }
}

@Serializable
class Garden(
    private val fertility: Float, val biome: Biome, val habitat: Habitat,
    private val rowType: Terrain.Type = Terrain.Type.TERRAIN_GRASS
) : Decor() {
    private fun sizeName() = if (room.width * room.height > 60) "farm field" else "garden"
    override fun description() = "A ${sizeName()} plowed in rows."
    override fun abandonedDescription() = "An abandoned ${sizeName()}, overgrown with weeds."
    override fun doFurnish() {
        val inVertRows = Dice.flip()
        val gardenDensity = fertility * 2f
        val gardenPlantSpawns = gardenPlantSpawns()
        for (tx in x0 until x1) {
            for (ty in y0 until y1) {
                if ((inVertRows && (tx % 2 == 0)) || (!inVertRows && (ty % 2 == 0)) || isAbandoned) {
                    setTerrain(tx, ty, rowType)
                    carto.getPlant(biome, habitat, 1f,
                        gardenDensity, gardenPlantSpawns)?.also { plant ->
                        spawnAt(tx, ty, plant)
                    }
                }
                if (!isAbandoned && carto is WorldCarto) (carto as WorldCarto).setFlag(tx, ty, WorldCarto.CellFlag.NO_PLANTS)
            }
        }
    }
}

@Serializable
class Graveyard(
    private val density: Float,
    private val slop: Float,
) : Decor() {
    override fun description() = "An old graveyard.  You can't help but whistle."
    override fun abandonedDescription() = "A forgotten graveyard, overgrown with weeds."
    override fun doFurnish() {
        for (tx in x0 until x1) {
            for (ty in y0 until y1) {
                if (tx % 3 == 0 && ty % 3 == 0 && Dice.chance(density)) {
                    val ox = if (Dice.chance(slop)) Dice.range(-1, 1) else 0
                    val oy = if (Dice.chance(slop)) Dice.range(-1, 1) else 0
                    if (boundsCheck(tx + ox, ty + oy)) spawnAt(tx + ox, ty + oy, Gravestone())
                }
                if (!isAbandoned && Dice.chance(0.95f)) flagsAt(tx, ty)?.add(WorldCarto.CellFlag.NO_PLANTS)
            }
        }
    }
}

@Serializable
class Stage : Decor() {
    override fun description() = "A broad raised platform for public gatherings."
    override fun abandonedDescription() = "A raised platform once used for public events, long abandoned."
    override fun doFurnish() {
        val terrain = listOf(Terrain.Type.TERRAIN_WOODFLOOR, Terrain.Type.TERRAIN_STONEFLOOR).random()
        for (tx in x0 until x1) {
            for (ty in y0 until y1) {
                setTerrain(tx, ty, terrain)
            }
        }
    }
}

@Serializable
class Barn : Decor() {
    override fun description() = "A barn.  It smells of hay and manure."
    override fun abandonedDescription() = "A barn that hasn't seen use in a long time."
    override fun doFurnish() {

    }
}