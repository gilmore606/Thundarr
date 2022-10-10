import actors.Player
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import input.KeyboardProcessor
import input.MouseProcessor
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ktx.app.KtxGame
import ktx.async.KtxAsync
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.*
import ktx.style.*
import render.GameScreen
import ui.Console
import util.log
import world.cartos.RoomyMaze
import world.Level
import java.io.File

object App : KtxGame<Screen>() {

    val player: Player = Player()
    lateinit var level: Level
    var turnTime = 0f

    lateinit var uiStage: Stage

    override fun create() {
        setupLog()
        KtxAsync.initiate()

        level = RoomyMaze.makeLevel()
        val playerStart = level.tempPlayerStart()
        level.director.add(player, playerStart.x, playerStart.y)

        addScreen(GameScreen)
        setScreen<GameScreen>()

        buildUI()

        log.info("Thundarr started.")
    }

    private fun buildUI() {
        Scene2DSkin.defaultSkin = skin {
            label {
                font = BitmapFont(FileHandle("src/main/resources/font/amstrad36.fnt"))
                fontColor = Color.YELLOW
            }
        }

        uiStage = Stage()
        uiStage.actors {
            // Root actor added directly to the stage - a table:
            table {
                // Table settings:
                setFillParent(true)
                // Table children:
                label("Hello world!")
                Console
            }
        }

        Gdx.input.inputProcessor = InputMultiplexer(KeyboardProcessor, MouseProcessor, uiStage)
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
}
