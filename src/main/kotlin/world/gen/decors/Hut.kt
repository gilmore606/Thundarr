package world.gen.decors

import things.*
import util.Dice

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
