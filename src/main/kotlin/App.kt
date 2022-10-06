import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import input.KeyboardProcessor
import input.MouseProcessor
import ktx.app.KtxGame
import ktx.app.KtxScreen
import mu.KotlinLogging
import render.GameScreen
import render.tilesets.DungeonTileSet
import render.tilesets.TileSet
import world.cartos.RoomyMaze
import world.cartos.TestCarto


object App : KtxGame<Screen>() {

    override fun create() {
        System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG")
        val log = KotlinLogging.logger {}
        log.info("Thundarr starting up!")

        // Set up inputs
        Gdx.input.inputProcessor = InputMultiplexer(KeyboardProcessor, MouseProcessor)

        // Create and show the level
        val firstLevel = RoomyMaze.makeLevel()

        addScreen(GameScreen)
        setScreen<GameScreen>()

        getScreen<GameScreen>().observeLevel(firstLevel)
        getScreen<GameScreen>().moveCenter(12, 8)
    }

    override fun dispose() {

    }


}
