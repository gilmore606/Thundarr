package world.gen.decors

import actors.jobs.*
import kotlinx.serialization.Serializable
import things.*
import util.Dice
import util.XY
import world.gen.biomes.Biome
import world.gen.cartos.Carto
import world.gen.cartos.WorldCarto
import world.gen.habitats.Habitat
import world.gen.gardenPlantSpawns
import world.terrains.Terrain
import java.lang.Math.max
import java.lang.Math.min

@Serializable
class Hut : Decor() {
    var bedLocations = mutableListOf<XY>()

    override fun description() = listOf(
        "A simple village hut.",
        "A single-room country dwelling.",
    ).random()
    override fun doFurnish() {
        val bedCount = max(1, min(3, (room.rect.area() / 12)))
        repeat (bedCount) {
            againstWall { spawn(if (Dice.chance(0.9f)) Bed() else Bedroll()) { bedXY ->
                bedLocations.add(bedXY)
            } }
        }
        againstWall { spawn(Candlestick())}
        chance(0.5f + bedCount * 0.1f) { againstWall { spawn(Trunk()) } }
        chance(0.2f + bedCount * 0.2f) { awayFromWall { spawn(Table()) } }
        chance(bedCount * 0.1f) { awayFromWall { spawn(Table()) } }
        if (clearCount > 20) {
            chance(0.6f) { againstWall { spawn(
                if (Dice.chance(0.7f)) FilingCabinet() else Bookshelf()
            )}}
        }
    }
    override fun job() = HomeJob(room.rect)
}

@Serializable
class Schoolhouse : Decor() {
    override fun description() = "A country schoolhouse lined with small desks."
    override fun abandonedDescription() = "An abandoned country schoolhouse, the small desks covered in dust and cobwebs."
    override fun fitsInRoom(room: Room) = room.width >= 5 && room.height >= 5

    override fun doFurnish() {
        againstWall { spawn(Candlestick())}
        repeat(Dice.oneTo(2)) { againstWall {
            spawn(Bookshelf())
            clearAround()
        } }
        forEachClear { x,y ->
            if (x % 2 == 0 && y % 2 == 0) spawn(Table())
        }
    }

    override fun job() = SchoolJob(room.rect)
}

@Serializable
class Church : Decor() {
    override fun description() = "A humble but lovingly constructed shrine to the Lords of Light."
    override fun abandonedDescription() = "An abandoned shrine to the Lords of Light, now covered in dust."
    override fun doFurnish() {
        againstWall { spawn(Candlestick())}
        atCenter { spawn(
            if (!isAbandoned || Dice.chance(0.5f)) Shrine() else Boulder()
        ) }
        repeat(Dice.zeroTo(2)) { againstWall { spawn(Table()) }}
    }
    override fun job() = ChurchJob(room.rect)
}

@Serializable
class StorageShed : Decor() {
    override fun description() = "A musty-smelling storage shed."

    override fun doFurnish() {
        againstWall { spawn(Candlestick())}
        repeat (Dice.range(4, 8)) {
            awayFromWall { spawn(if (Dice.flip()) Trunk() else FilingCabinet()) }
        }
        if (Dice.flip()) repeat (Dice.range(1, 3)) {
            againstWall { spawn(Table()) }
        }
    }

    override fun job() = WorkJob(
        "storage shed", "worker", room.rect, false, false,
        setOf("Time for some warehouse work.", "I'm going to the storage shed.", "I'll get it from storage.",
            "They need me in the warehouse today.", "I'll go help out at the warehouse.")
    )
}

@Serializable
class BlacksmithShop : Decor() {
    var caseKey: Thing.Key? = null

    override fun description() = "A blacksmith's shop."
    override fun abandonedDescription() = "An abandoned blacksmith's shop."
    override fun doFurnish() {
        againstWall { spawn(Candlestick())}
        againstWall {
            caseKey = spawn(StorageCabinet())?.getKey()
        }
        forArea(x0+1, y0+1, x1-1, y1-1) { x,y ->
            setTerrain(x, y, Terrain.Type.TERRAIN_DIRT, roofed = true)
        }
        atCenter { spawn(Forge()) }
        repeat (Dice.range(1, 4)) {
            againstWall { spawn(Table()) }
        }
    }
    override fun job() = caseKey?.let { caseKey ->
        ForgeJob(room.rect, caseKey)
    } ?: super.job()
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
    override fun job() = FarmJob(sizeName(), room.rect)
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
    override fun job() = WorkJob(
        "graveyard", "penitent", room.rect, false, true, setOf(
            "The graves of my forefathers are sacred.",
            "I come here to think...and to remember.",
        )
    )
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
        againstWall { spawn(Lamppost()) }
    }
    override fun job() = WorkJob(
        "square", "performer", room.rect, false, true, setOf(
            "Hey look at me!  I'm a barbarian!  RAAR...just kidding.",
            "Work hard my friends, for the wizard may come at any time!",
            "Fear the wizard, and his evil works!",
            "My friends, keep faith with the Lords of Light!"
        )
    )
}

@Serializable
class Barn : Decor() {
    override fun description() = "A barn.  It smells of hay and manure."
    override fun abandonedDescription() = "A barn that hasn't seen use in a long time."
    override fun doFurnish() {
        againstWall { spawn(Candlestick())}
    }
    override fun job() = WorkJob(
        "barn", "farmer", room.rect, false, true, setOf(
            "Nobody likes cleaning the barn.", "Animals, why are you so smelly?"
        )
    )
}

@Serializable
class Tavern(val name: String) : Decor() {
    var caseKey: Thing.Key? = null

    override fun description() = "A roadside inn: \"$name\"."
    override fun abandonedDescription() = "An abandoned inn."
    override fun doFurnish() {
        againstWall { spawn(Candlestick())}
        againstWall { spawn(Candlestick())}
        againstWall { caseKey = spawn(StorageCabinet())?.getKey() }

        repeat (Dice.range(5, 8)) {
            awayFromWall {
                spawn(Table())
                clearAround()
            }
        }
    }
    override fun job() = caseKey?.let { caseKey ->
        TavernJob(name, room.rect, caseKey)
    } ?: super.job()
}

@Serializable
class TavernLoiterArea(val name: String): Decor() {
    override fun description() = "The ground around $name is well trodden."
    override fun doFurnish() {
        againstWall { spawn(Lamppost()) }
    }
    override fun job() = TavernLoiterJob(name, room.rect)
}

@Serializable
class Barracks(val vertical: Boolean) : Decor() {
    var bedLocations = mutableListOf<XY>()
    override fun description() = "A dormitory barracks."
    override fun doFurnish() {
        repeat (8) { awayFromWall {
            spawn(Bed()) { bedXY ->
                bedLocations.add(bedXY)
            }
        }}
        againstWall { spawn(Candlestick()) }
        againstWall { spawn(Trunk()) }
    }
}
