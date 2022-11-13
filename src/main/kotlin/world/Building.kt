package world

import kotlinx.serialization.Serializable
import util.UUID
import util.XY
import kotlin.random.Random

@Serializable
sealed class Building {
    val id: String = UUID()
    val xy = XY(0,0)
    val floorDimensions = XY(0, 0)
    open fun floorWidth() = floorDimensions.x
    open fun floorHeight() = floorDimensions.y
    var firstLevelId: String = UUID()
    open fun doorMsg() = "A non-descript door.  Go inside?"

    open fun at(x: Int, y: Int): Building {
        xy.x = x
        xy.y = y
        return this
    }
}

@Serializable
class BoringBuilding : Building() {
    override fun doorMsg() = "A rusty metal door stands slightly ajar.  Step inside?"
    override fun at(x: Int, y: Int): Building {
        floorDimensions.x = 30 + Random.nextInt(0, 40)
        floorDimensions.y = 30 + Random.nextInt(0, 40)
        return super.at(x, y)
    }
}
