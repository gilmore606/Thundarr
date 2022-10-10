import actors.Player
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.scenes.scene2d.Stage
import com.kotcrab.vis.ui.VisUI
import input.KeyboardProcessor
import input.MouseProcessor
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ktx.app.KtxGame
import ktx.async.KtxAsync
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.*
import render.GameScreen
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

        VisUI.load()
        Scene2DSkin.defaultSkin = VisUI.getSkin()
        uiStage = Stage()
        uiStage.actors {
            // Root actor added directly to the stage - a table:
            table {
                // Table settings:
                setFillParent(true)
                // Table children:
                label("Hello world!")
            }
        }

        Gdx.input.inputProcessor = InputMultiplexer(KeyboardProcessor, MouseProcessor, uiStage)

        log.info("Thundarr started.")

        KtxAsync.launch {
            File("savegame/level.json").writeText(Json.encodeToString(level))
            log.info("Saved to disk")
        }
    }

    override fun dispose() {
        log.info("Thundarr shutting down.")
        GameScreen.dispose()
    }

    private fun setupLog() {
        System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG")
        System.setProperty(org.slf4j.simple.SimpleLogger.SHOW_DATE_TIME_KEY, "true")
        System.setProperty(org.slf4j.simple.SimpleLogger.DATE_TIME_FORMAT_KEY, "hh:mm:ss.SSS")
        System.setProperty(org.slf4j.simple.SimpleLogger.SHOW_LOG_NAME_KEY, "false")
        System.setProperty(org.slf4j.simple.SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "false")
        log.info("Thundarr starting up!")
    }

    private fun saveState() {

    }

    private fun restoreState() {

    }
}
