import actors.Player
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import input.KeyboardProcessor
import input.MouseProcessor
import ktx.app.KtxGame
import render.GameScreen
import util.log
import world.cartos.RoomyMaze
import world.Level

object App : KtxGame<Screen>() {

    val player: Player = Player()
    lateinit var level: Level

    override fun create() {
        System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG")
        System.setProperty(org.slf4j.simple.SimpleLogger.SHOW_DATE_TIME_KEY, "true")
        System.setProperty(org.slf4j.simple.SimpleLogger.DATE_TIME_FORMAT_KEY, "hh:mm:ss.SSS")
        System.setProperty(org.slf4j.simple.SimpleLogger.SHOW_LOG_NAME_KEY, "false")
        System.setProperty(org.slf4j.simple.SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "false")

        log.info("Thundarr starting up!")

        Gdx.input.inputProcessor = InputMultiplexer(KeyboardProcessor, MouseProcessor)

        level = RoomyMaze.makeLevel()
        level.director.add(player, 12, 8)

        addScreen(GameScreen)
        setScreen<GameScreen>()

        log.info("Thundarr started.")
    }

    override fun dispose() {
        log.info("Thundarr shutting down.")
        GameScreen.dispose()
    }
}
