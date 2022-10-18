import actors.Player
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import ui.input.Keyboard
import ui.input.Mouse
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ktx.app.KtxGame
import ktx.async.KTX
import ktx.async.KtxAsync
import render.GameScreen
import ui.modals.ConfirmModal
import ui.modals.ControlsModal
import ui.panels.ConsolePanel
import ui.modals.CreditsModal
import ui.panels.StatusPanel
import util.gzipCompress
import util.gzipDecompress
import util.log
import world.*
import java.io.File
import kotlin.system.exitProcess

object App : KtxGame<Screen>() {

    @Serializable
    data class WorldState(
        val level: Level,
        val player: Player,
        val turnTime: Float,
        val zoom: Double,
    )
    const val saveFileName = "worldstate"
    const val saveFileFolder = "savegame"

    lateinit var player: Player
    lateinit var level: Level
    lateinit var saveState: SaveState
    var turnTime = 0f

    private var pendingJob: Job? = null

    var DEBUG_VISIBLE = false
        set(value) {
            field = value
            if (value) {
                GameScreen.addPanel(StatusPanel)
            } else {
                GameScreen.removePanel(StatusPanel)
            }
        }

    override fun create() {
        KtxAsync.initiate()
        setupLog()

        saveState = SaveState("myworld")

        if (saveState.worldExists()) {
            log.info("Loading saved state...")
            restoreState()
        } else {
            log.info("No saved state found, creating new world...")
            createNewWorld()
        }

        addScreen(GameScreen)
        setScreen<GameScreen>()
        GameScreen.addPanel(ConsolePanel)

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

        saveState.putWorldState(
            WorldState(
                level = level,
                player = player,
                turnTime = turnTime,
                zoom = GameScreen.zoom
            )
        )
    }

    private fun restoreState() {
        val state = saveState.getWorldState()
        level = state.level
        player = state.player
        turnTime = state.turnTime
        GameScreen.zoom = state.zoom
        GameScreen.lastPov.x = player.xy.y
        GameScreen.lastPov.y = player.xy.y
        level.director.add(player, player.xy.x, player.xy.y, level)
        level.onRestore()
        log.info("Restored state with player at ${player.xy.x} ${player.xy.y}.")
    }

    private fun createNewWorld() {
        saveState.eraseAll()
        level = WorldLevel()
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
            turnTime = 0f
            ConsolePanel.say("You step tentatively into the apocalypse...")
        }
    }

    fun restartWorld() {
        GameScreen.addModal(ConfirmModal(
                listOf(
                    "Are you sure you want to abandon this world?",
                    "All your progress, such as it is, will be lost."),
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
                listOf("Are you sure you want to save and exit the game?"),
                "Save and exit", "Cancel"
            ) { yes ->
                if (yes) {
                    KtxAsync.launch {
                        saveState()
                        log.info("Waiting for ChunkLoader to finish...")
                        // TODO: actually wait for loader to finish.  why doesn't that work?
                        delay(500)
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

}
