package world

import actors.stats.Heart
import kotlinx.serialization.Serializable
import render.Screen
import util.*
import world.gen.cartos.CavernCarto
import world.gen.cartos.LevelCarto
import world.journal.JournalEntry
import world.level.EnclosedLevel
import kotlin.random.Random

@Serializable
sealed class Building {
    val id: String = UUID()
    val xy = XY(0,0)
    val facing = XY(0, 0)
    val floorDimensions = XY(0, 0)
    var threatLevel = 0
    open fun shortName() = "boring building"
    open fun floorWidth() = floorDimensions.x
    open fun floorHeight() = floorDimensions.y
    var firstLevelId: String = UUID()
    open fun doorMsg() = "A non-descript door.  Go inside?"

    open val lightColorHalls = LightColor(0.5f, 0.2f, 0.3f)
    open val lightColorRooms = LightColor(0.1f, 0.6f, 0.4f)
    open val lightColorSpecial = LightColor(0.6f, 0.3f, 0f)
    open val lightColorVariance = 0.15f
    open val lightVariance = 0.2f
    open val lightAttempts = 1000

    open fun threat(threat: Int): Building {
        threatLevel = threat
        return this
    }
    open fun at(x: Int, y: Int): Building {
        xy.x = x
        xy.y = y
        return this
    }
    open fun facing(xy: XY): Building {
        facing.x = xy.x
        facing.y = xy.y
        return this
    }

    open fun carveLevel(level: EnclosedLevel) {
        LevelCarto(0, 0, floorWidth() - 1, floorHeight() - 1, level, this)
            .carveLevel(
                worldDest = XY(xy.x + facing.x, xy.y + facing.y)
            )
    }

    open fun onPlayerExited() { }
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

    override fun shortName() = "$wizardName's lair"

    override fun doorMsg() = "An imposing wrought iron gateway bearing the inscription '$wizardFullName' stands open.  Step inside?"
    override fun at(x: Int, y: Int): Building {
        floorDimensions.x = 30 + Random.nextInt(0, 40)
        floorDimensions.y = 30 + Random.nextInt(0, 40)
        return super.at(x, y)
    }
}

@Serializable
class StarterDungeon : WizardDungeon() {
    override fun carveLevel(level: EnclosedLevel) {
        super.carveLevel(level)
        log.info("StarterDungeon $this exists - finishing create world")
        App.finishCreateWorld(level, App.StartType.ESCAPE, this)
    }

    override fun onPlayerExited() {
        App.player.journal.achieve(JournalEntry(
            "Freedom!",
            "Today the sun shines on a free barbarian!  I've escaped the gates of ${wizardName}'s foul lair and cast off the bonds of slavery forever.  But what of those who remain?  I must gather my strength and return one day.  For the captive victims of $wizardName, and those of all the wizards, from Man-Hat to Los Fisgo!"
        ), withModal = true)
        Heart.improve(App.player, fullLevel = true)
    }
}

@Serializable
class NaturalCavern : Building() {
    override fun shortName() = "cavern"
    override fun doorMsg() = "A tunnel slopes down underground.  Venture in?"
    override fun at(x: Int, y: Int): Building {
        floorDimensions.x = 30 + Random.nextInt(0, 60)
        floorDimensions.y = 30 + Random.nextInt(0, 60)
        return super.at(x, y)
    }
    override fun carveLevel(level: EnclosedLevel) {
        CavernCarto(level, this).carveLevel(
            worldDest = XY(xy.x + facing.x, xy.y + facing.y)
        )
    }
}
