package world

import util.log

object LevelKeeper {

    class LiveLevel(
        val level: Level,
        val addedAtMs: Long,
        val lastEnteredAtMs: Long
    )

    private const val maxLevelsToKeep = 6

    val liveLevels = mutableSetOf<LiveLevel>()

    // Get the specified level from the live list, or start it up and add it.
    // Remove old levels if necessary.
    fun getLevel(levelId: String): Level {
        liveLevels.forEach { liveLevel ->
            if (liveLevel.level.levelId() == levelId) {
                return liveLevel.level
            }
        }

        val level = Level.make(levelId)
        liveLevels.add(LiveLevel(
            level = level,
            addedAtMs = System.currentTimeMillis(),
            lastEnteredAtMs = 0
        ))
        pruneLevels()
        return level
    }

    // Discard old levels if we're holding too many active.
    private fun pruneLevels() {
        if (liveLevels.size > maxLevelsToKeep) {
            liveLevels.sortedByDescending { it.lastEnteredAtMs }.forEach {
                if (liveLevels.size > maxLevelsToKeep) {
                    hibernateLevel(it.level)
                }
            }
        }
    }

    // Save and drop all levels in prep for exiting/restarting.
    fun hibernateAll() {
        mutableSetOf<LiveLevel>().apply { addAll(liveLevels) }.forEach { hibernateLevel(it.level) }
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
