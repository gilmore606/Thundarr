import actors.Player
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import ui.input.Keyboard
import ui.input.Mouse
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ktx.app.KtxGame
import ktx.async.KtxAsync
import render.GameScreen
import ui.modals.ConfirmModal
import ui.panels.ConsolePanel
import ui.modals.CreditsModal
import ui.panels.StatusPanel
import util.gzipCompress
import util.gzipDecompress
import util.log
import world.Chunk
import world.EnclosedLevel
import world.Level
import world.WorldLevel
import java.io.File
import kotlin.system.exitProcess

object App : KtxGame<Screen>() {

    @Serializable
    data class SaveState(
        val level: Level,
        val player: Player,
        val turnTime: Float,
        val zoom: Double,
    )
    const val saveFileName = "worldstate"
    const val saveFileFolder = "savegame"

    lateinit var player: Player
    lateinit var level: Level
    var turnTime = 0f

    var DEBUG_VISIBLE = false

    override fun create() {
        setupLog()
        KtxAsync.initiate()

        if (hasSavedState()) {
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
    }

    private fun setupLog() {
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

    private fun hasSavedState(): Boolean = File("$saveFileFolder/$saveFileName.json.gz").exists()

    private fun saveState() {
        KtxAsync.launch {
            val state = SaveState(level, player, turnTime, GameScreen.zoom)
            File("$saveFileFolder/$saveFileName.json.gz").writeBytes(Json.encodeToString(state).gzipCompress())
            log.info("Saved state.")
        }
    }

    private fun restoreState() {
        val state = Json.decodeFromString<SaveState>(File("$saveFileFolder/$saveFileName.json.gz").readBytes().gzipDecompress())
        level = state.level
        player = state.player
        turnTime = state.turnTime
        GameScreen.zoom = state.zoom
        level.director.add(player, player.xy.x, player.xy.y, level)
        level.onRestore()
        log.info("Restored state with player at ${player.xy.x} ${player.xy.y}.")
    }

    private fun createNewWorld() {
        if (false) {
            level = EnclosedLevel(200, 200)
        } else {
            level = WorldLevel()
        }

        File("$saveFileFolder/$saveFileName.json.gz").delete()
        Chunk.allFiles()?.forEach { it.delete() }

        player = Player()
        val playerStart = level.tempPlayerStart()
        level.director.add(player, playerStart.x, playerStart.y, level)
        turnTime = 0f

        ConsolePanel.say("You step tentatively into the apocalypse...")
    }

    fun restartWorld() {
        GameScreen.addModal(
            ConfirmModal(
                listOf(
                    "Are you sure you want to abandon this world?",
                    "All your progress, such as it is, will be lost."),
                "Abandon", "Cancel"
            ) { yes ->
                if (yes) {
                    ConsolePanel.say("You abandon the world.")
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
                    level.unload()
                    saveState()
                    dispose()
                    exitProcess(0)
                } else {
                    ConsolePanel.say("You remember that one thing you needed to do...")
                }
            }
        )
    }

}
