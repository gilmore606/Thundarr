import actors.Player
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import ui.input.KeyboardProcessor
import ui.input.MouseProcessor
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ktx.app.KtxGame
import ktx.async.KtxAsync
import ktx.freetype.*
import render.GameScreen
import ui.Console
import ui.modals.CreditsModal
import util.log
import world.cartos.RoomyMaze
import world.Level
import java.io.File

object App : KtxGame<Screen>() {

    val player: Player = Player()
    lateinit var level: Level
    var turnTime = 0f

    override fun create() {
        setupLog()
        KtxAsync.initiate()

        level = RoomyMaze.makeLevel()
        val playerStart = level.tempPlayerStart()
        level.director.add(player, playerStart.x, playerStart.y)

        addScreen(GameScreen)
        setScreen<GameScreen>()

        buildUI()

        Console.say("The moon is broken in god-damned half!")
    }

    private fun buildUI() {
        GameScreen.addPanel(Console)

        Gdx.input.inputProcessor = InputMultiplexer(KeyboardProcessor, MouseProcessor)
    }

    private fun setupLog() {
        System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG")
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
        KtxAsync.launch {
            File("savegame/level.json").writeText(Json.encodeToString(level))
            log.info("Saved to disk")
        }
    }

    private fun restoreState() {

    }

    fun restartWorld() {
        Console.say("You abandon the world.")
    }

    fun openSettings() {

    }

    fun openControls() {

    }

    fun openCredits() {
        GameScreen.addModal(CreditsModal())
    }

    fun saveAndQuit() {
        dispose()
        System.exit(0)
    }

}
