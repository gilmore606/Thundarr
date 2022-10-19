import actors.Player
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import kotlinx.coroutines.*
import ui.input.Keyboard
import ui.input.Mouse
import kotlinx.serialization.Serializable
import ktx.app.KtxGame
import ktx.async.KTX
import ktx.async.KtxAsync
import render.GameScreen
import ui.modals.ConfirmModal
import ui.modals.ControlsModal
import ui.panels.ConsolePanel
import ui.modals.CreditsModal
import ui.modals.SavingModal
import ui.panels.DebugPanel
import ui.panels.StatusPanel
import util.log
import world.*
import kotlin.system.exitProcess

object App : KtxGame<Screen>() {

    @Serializable
    data class WorldState(
        val level: Level,
        val player: Player,
        val time: Double,
        val zoomIndex: Double,
    )

    lateinit var player: Player
    lateinit var level: Level
    lateinit var worldLevel: WorldLevel
    lateinit var save: SaveState
    var time: Double = 0.0
    var lastHour = -1
    var timeString: String = "???"
    var dateString: String = "???"

    private var pendingJob: Job? = null

    var DEBUG_VISIBLE = false
        set(value) {
            field = value
            if (value) {
                GameScreen.addPanel(DebugPanel)
            } else {
                GameScreen.removePanel(DebugPanel)
            }
        }

    override fun create() {
        KtxAsync.initiate()
        setupLog()

        save = SaveState("myworld")

        if (save.worldExists()) {
            log.info("Loading saved state...")
            restoreState()
        } else {
            log.info("No saved state found, creating new world...")
            createNewWorld()
        }

        addScreen(GameScreen)
        setScreen<GameScreen>()
        GameScreen.addPanel(ConsolePanel)
        GameScreen.addPanel(StatusPanel)

        Gdx.input.inputProcessor = InputMultiplexer(Keyboard, Mouse)

        ConsolePanel.say("The moon is broken in god-damned half!")
        ConsolePanel.say("\"Demon dogs!\"")
    }

    private fun setupLog() {
        Dispatchers.KTX.mainThread.name = "main"
        System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO")
        System.setProperty(org.slf4j.simple.SimpleLogger.SHOW_DATE_TIME_KEY, "true")
        System.setProperty(org.slf4j.simple.SimpleLogger.DATE_TIME_FORMAT_KEY, "hh:mm:ss.SSS")
        System.setProperty(org.slf4j.simple.SimpleLogger.SHOW_LOG_NAME_KEY, "false")
        System.setProperty(org.slf4j.simple.SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "false")
        log.info("Thundarr starting up!")
    }

    override fun dispose() {
        log.info("Thundarr shutting down.")
        GameScreen.dispose()
    }

    private fun saveState() {
        level.unload()

        save.putWorldState(
            WorldState(
                level = level,
                player = player,
                time = time,
                zoomIndex = GameScreen.zoomIndex
            )
        )
    }

    private fun restoreState() {
        val state = save.getWorldState()
        level = state.level
        if (level is WorldLevel) worldLevel = level as WorldLevel
        player = state.player
        updateTime(state.time)
        GameScreen.restoreZoomIndex(state.zoomIndex)
        GameScreen.lastPov.x = player.xy.y
        GameScreen.lastPov.y = player.xy.y
        level.director.add(player, player.xy.x, player.xy.y, level)
        updateTime(time, true)
        level.onRestore()
        log.info("Restored state with player at ${player.xy.x} ${player.xy.y}.")
    }

    private fun createNewWorld() {
        save.eraseAll()
        level = WorldLevel()
        worldLevel = level as WorldLevel
        player = Player()
        level.setPov(200, 200)
        pendingJob = KtxAsync.launch {
            var playerStart = level.tempPlayerStart()
            var waitMs = 0
            while (playerStart == null) {
                log.debug("Waiting for chunks...")
                delay(20)
                waitMs += 20
                playerStart = level.tempPlayerStart()
            }
            log.info("Waited $waitMs ms for start chunk.")
            level.director.add(player, playerStart.x, playerStart.y, level)
            updateTime(0.0)
            ConsolePanel.say("You step tentatively into the apocalypse...")
        }
    }

    fun restartWorld() {
        GameScreen.addModal(ConfirmModal(
                listOf(
                    "Abandon this world?",
                    "All your progress will be lost."),
                "Abandon", "Cancel"
            ) { yes ->
                if (yes) {
                    ConsolePanel.say("You abandon the world.")
                    pendingJob?.cancel()
                    createNewWorld()
                } else {
                    ConsolePanel.say("You gather your resolve and carry on.")
                }
            }
        )
    }

    fun openSettings() {

    }

    fun openControls() {
        GameScreen.addModal(ControlsModal())
    }

    fun openCredits() {
        GameScreen.addModal(CreditsModal())
    }

    fun saveAndQuit() {
        GameScreen.addModal(
            ConfirmModal(
                listOf("Quit the game?", "Your progress will be saved."),
                "Quit", "Cancel"
            ) { yes ->
                if (yes) {
                    GameScreen.addModal(SavingModal())
                    KtxAsync.launch {
                        delay(200)
                        saveState()
                        while (ChunkLoader.isWorking()) {
                            log.info("Waiting for ChunkLoader to finish...")
                            delay(100)
                        }
                        log.info("State saved.")
                        dispose()
                        exitProcess(0)
                    }
                } else {
                    ConsolePanel.say("You remember that one thing you needed to do...")
                }
            }
        )
    }

    fun moveToLevel(levelId: String) {
        // do something with current this.level to save/unload it?
        level.director.remove(player)

        level = EnclosedLevel(levelId)

        KtxAsync.launch {
            var playerStart = level.tempPlayerStart()
            while (playerStart == null) {
                log.debug("Waiting for level...")
                delay(50)
                playerStart = level.tempPlayerStart()
            }
            level.director.add(player, playerStart.x, playerStart.y, level)
            updateTime(time, true)
            ConsolePanel.say("You cautiously step inside...")
        }
    }

    fun passTime(passed: Float) {
        updateTime(time + passed.toDouble())
    }

    private fun updateTime(newTime: Double, force: Boolean = false) {
        val dayLength = 1000.0
        val monthNames = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        val yearZero = 2994

        time = newTime
        val day = (time / dayLength).toInt()
        val timeOfDay = time - (day * dayLength)
        val minutes = (timeOfDay / dayLength) * 1440.0
        val hour = (minutes / 60).toInt()
        val minute = minutes.toInt() - (hour * 60)
        var ampm = "am"
        var amhour = hour
        if (hour >= 11) {
            ampm = "pm"
            if (hour >= 12) {
                amhour -= 12
            }
        }
        amhour += 1
        val minstr = if (minute < 10) "0$minute" else "$minute"

        val year = (day / 360)
        val yearDay = day - (year * 360)
        val month = yearDay / 30
        val monthDay = (yearDay - month * 30) + 1
        val monthName = monthNames[month]
        val realYear = yearZero + year

        timeString = "$amhour:$minstr $ampm"
        dateString = "$monthName $monthDay, $realYear"

        if (lastHour != hour || force) {
            lastHour = hour
            App.level.updateAmbientLight(hour)
        }
    }
}
