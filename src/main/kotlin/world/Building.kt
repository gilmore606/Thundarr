package world

import kotlinx.serialization.Serializable
import util.*
import world.cartos.LevelCarto
import world.level.Level
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

    open fun generateLevelChunk(level: Level, chunk: Chunk) {
        LevelCarto(0, 0, floorWidth() - 1, floorHeight() - 1, chunk, level)
            .carveLevel(
                worldExit = LevelCarto.WorldExit(NORTH, XY(xy.x, xy.y - 1))
            )
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

@Serializable
sealed class WizardDungeon : Building() {
    val wizardName = Madlib.wizardName()
    val wizardFullName = Madlib.wizardFullName(wizardName)

    override fun doorMsg() = "An imposing wrought iron gateway bearing the inscription '$wizardFullName' stands open.  Step inside?"
    override fun at(x: Int, y: Int): Building {
        floorDimensions.x = 30 + Random.nextInt(0, 40)
        floorDimensions.y = 30 + Random.nextInt(0, 40)
        return super.at(x, y)
    }
}

@Serializable
class StarterDungeon : WizardDungeon() {

    override fun generateLevelChunk(level: Level, chunk: Chunk) {
        super.generateLevelChunk(level, chunk)
        log.info("StarterDungeon $this exists - finishing create world")
        App.finishCreateWorld(level, App.StartType.ESCAPE, this)
    }

}
