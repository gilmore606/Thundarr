package world.gen.decors

import kotlinx.serialization.Serializable
import things.*
import util.Dice
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
        atCenter { spawn(Shrine()) }
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
