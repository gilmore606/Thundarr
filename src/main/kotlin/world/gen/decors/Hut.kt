package world.gen.decors

import things.Bedroll
import things.Table
import things.Trunk
import util.Dice

object Hut : Decor() {
    override fun furniture() = when (Dice.oneTo(3)) {
        1 -> Table()
        2 -> Bedroll()
        3 -> Trunk()
        else -> Trunk()
    }
}
