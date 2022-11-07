package world.persist

import kotlinx.coroutines.delay
import render.Screen
import ui.panels.Toolbar
import util.XY
import util.log
import world.Building
import world.level.Level
import world.level.WorldLevel
import java.util.concurrent.ConcurrentSkipListSet

object LevelKeeper {

    class LiveLevel(
        val level: Level,
        val addedAtMs: Long,
        var lastAccessedAt: Long
    ) : Comparable<LiveLevel> {
        override fun compareTo(other: LiveLevel): Int {
            if (other.level == this.level) return 0
            else if (other.addedAtMs > this.addedAtMs) return 1
            else return -1
        }
    }

    private const val maxLevelsToKeep = 8

    val liveLevels = ConcurrentSkipListSet<LiveLevel>()

    fun forEachLiveLevel(doThis: (Level)->Unit) {
        liveLevels.forEach { liveLevel ->
            doThis(liveLevel.level)
        }
    }

    // Get the specified level from the live list, or start it up and add it.
    // Remove old levels if necessary.
    fun getLevel(levelId: String): Level {
        liveLevels.forEach { liveLevel ->
            if (liveLevel.level.levelId() == levelId) {
                liveLevel.lastAccessedAt = System.currentTimeMillis()
                return liveLevel.level
            }
        }

        val level = Level.make(levelId)
        liveLevels.add(
            LiveLevel(
            level = level,
            addedAtMs = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        )
        )

        pruneLevels()
        return level
    }

    fun getWarmedWorld(around: XY): Level = getLevel("world").apply { setPov(around.x, around.y) }

    fun makeBuilding(building: Building) {
        App.save.putBuilding(building)
        getLevel(building.firstLevelId)
    }

    // Discard old non-world levels if we're holding too many active.
    private fun pruneLevels() {
        if (liveLevels.size > maxLevelsToKeep) {
            liveLevels.sortedBy { it.lastAccessedAt }.filter { it.level !is WorldLevel }.forEach {
                if (liveLevels.size > maxLevelsToKeep) {
                    hibernateLevel(it.level)
                }
            }
        }
    }

    // Save and drop all levels in prep for exiting/restarting.
    private fun hibernateAllAsync() {
        mutableSetOf<LiveLevel>().apply { addAll(liveLevels) }.forEach { hibernateLevel(it.level) }
        liveLevels.clear()
    }

    suspend fun hibernateAll() {
        hibernateAllAsync()
        while (ChunkLoader.isWorking()) {
            log.info("Waiting for ChunkLoader to finish...")
            delay(100)
        }
    }

    // Unload and remove the specified level from the live list.
    private fun hibernateLevel(level: Level) {
        log.info("Hibernating level $level")
        level.unload()
        liveLevels.removeIf { it.level == level }
    }

    // Run action queues for all live levels.  This happens on every render.
    fun runActorQueues() {
        mutableSetOf<LiveLevel>().apply { addAll(liveLevels) }.forEach {
            it.level.director.runQueue(it.level)
        }
    }

    // Distribute the passage of player action juice to everyone that acts.
    fun advanceJuice(juice: Float) {
        liveLevels.forEach {
            it.level.director.advanceJuice(juice)
        }
    }

    // Distribute the passage of time to everyone that cares.
    fun advanceTime(turns: Float) {
        App.updateTime(App.time + turns.toDouble())
        Screen.advanceTime(turns)
        Toolbar.refresh()
        liveLevels.forEach {
            it.level.advanceTime(turns)
        }
    }

}
