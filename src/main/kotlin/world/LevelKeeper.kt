package world

import kotlinx.coroutines.delay
import util.log

object LevelKeeper {

    class LiveLevel(
        val level: Level,
        val addedAtMs: Long,
        var lastAccessedAt: Long
    )

    private const val maxLevelsToKeep = 6

    val liveLevels = mutableSetOf<LiveLevel>()

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
        liveLevels.add(LiveLevel(
            level = level,
            addedAtMs = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        ))

        pruneLevels()
        return level
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
        liveLevels.forEach { it.level.director.runQueue(it.level) }
    }

}
