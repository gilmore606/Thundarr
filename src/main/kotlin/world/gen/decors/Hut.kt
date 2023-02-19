package world.gen.decors

import things.*
import util.Dice
import world.terrains.Terrain

class Hut : Decor() {
    override fun doFurnish() {
        againstWall { spawn(Bedroll()) }
        chance(0.3f) { againstWall { spawn(Trunk()) } }
        chance(0.6f) { awayFromWall { spawn(Table()) } }
    }
}

class HutBedroom : Decor() {
    override fun doFurnish() {
        againstWall { spawn(Bedroll()) }
        chance(0.4f) { againstWall { spawn(Trunk()) } }
    }
}

class HutLivingRoom : Decor() {
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

class Schoolhouse : Decor() {
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

class Church : Decor() {
    override fun doFurnish() {
        atCenter { spawn(Shrine()) }
        repeat(Dice.zeroTo(2)) { againstWall { spawn(Table()) }}
    }
}

class StorageShed : Decor() {
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

class BlacksmithShop : Decor() {
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
