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
import things.Apple
import things.Sunsword
import things.Torch
import ui.modals.*
import ui.panels.ConsolePanel
import ui.panels.LeftButtons
import ui.panels.LookPanel
import ui.panels.StatusPanel
import util.XY
import util.log
import world.*
import kotlin.system.exitProcess

const val RESOURCE_FILE_DIR = "src/main/resources/"

object App : KtxGame<Screen>() {

    @Serializable
    data class WorldState(
        val levelId: String,
        val player: Player,
        val time: Double,
        val zoomIndex: Double,
    )

    private const val TURNS_PER_DAY = 2000.0
    private const val YEAR_ZERO = 2994

    lateinit var player: Player
    lateinit var level: Level
    lateinit var save: SaveState

    var time: Double = 0.0
    var lastHour = -1
    var timeString: String = "???"
    var dateString: String = "???"

    private var pendingJob: Job? = null

    var DEBUG_VISIBLE = false
    var DEBUG_PANEL = false

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
        GameScreen.addPanel(LookPanel)
        GameScreen.addPanel(LeftButtons)

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

    private fun saveStateAndExitProcess() {
        pendingJob = KtxAsync.launch {
            LevelKeeper.hibernateAll()

            save.putWorldState(
                WorldState(
                    levelId = level.levelId(),
                    player = player,
                    time = time,
                    zoomIndex = GameScreen.zoomIndex
                )
            )

            log.info("State saved.")
            dispose()
            exitProcess(0)
        }
    }

    private fun restoreState() {
        pendingJob = KtxAsync.launch {
            LevelKeeper.hibernateAll()

            val state = save.getWorldState()
            level = LevelKeeper.getLevel(state.levelId)
            player = state.player
            player.onRestore()
            level.setPov(player.xy.x, player.xy.y)

            updateTime(state.time)
            GameScreen.restoreZoomIndex(state.zoomIndex)
            while (!level.isReady()) {
                log.info("Waiting for level...")
                delay(100)
            }
            movePlayerIntoLevel(player.xy.x, player.xy.y, level)
            log.info("Restored state with player at ${player.xy.x} ${player.xy.y}.")
        }
    }

    private fun createNewWorld() {
        GameScreen.addModal(LoadingModal("The moon...it's breaking in half!"))
        pendingJob = KtxAsync.launch {
            LevelKeeper.hibernateAll()
            save.eraseAll()

            level = LevelKeeper.getLevel("world")
            player = Player()
            Sunsword().moveTo(player)
            Torch().moveTo(player)
            Torch().moveTo(player)
            Apple().moveTo(player)

            updateTime(0.0)
            level.setPov(200, 200)

            var playerStart: XY? = null
            while (playerStart == null) {
                log.debug("Waiting for chunks...")
                delay(50)
                playerStart = level.getPlayerEntranceFrom(level.levelId())
            }
            movePlayerIntoLevel(playerStart.x, playerStart.y, null)
            ConsolePanel.say("You step tentatively into the apocalypse...")
        }
    }



    fun openSettings() {

    }

    fun openControls() {
        GameScreen.addModal(ControlsModal())
    }

    fun openCredits() {
        GameScreen.addModal(CreditsModal())
    }

    fun enterLevelFromWorld(levelId: String) {
        level.director.detachActor(player)
        val oldLevelId = level.levelId()
        level = LevelKeeper.getLevel(levelId)
        KtxAsync.launch {
            while (!level.isReady()) {
                log.debug("Waiting for level...")
                delay(50)
            }
            val entrance = level.getPlayerEntranceFrom(oldLevelId)
            movePlayerIntoLevel(entrance!!.x, entrance!!.y, LevelKeeper.getLevel(oldLevelId))
            ConsolePanel.say("You cautiously step inside...")
        }
    }

    fun enterWorldFromLevel(dest: XY) {
        level.director.detachActor(player)
        val oldLevel = level
        level = LevelKeeper.getLevel("world")

        KtxAsync.launch {
            while (!level.isReady()) {
                log.debug("Waiting for world...")
                delay(50)
            }
            movePlayerIntoLevel(dest.x, dest.y, oldLevel)
            ConsolePanel.say("You step back outside to face the wilderness once again.")
        }
    }

    private fun movePlayerIntoLevel(x: Int, y: Int, from: Level?) {
        player.moveTo(level, x, y, from)
        updateTime(time)
        level.onRestore()
        GameScreen.mouseScrolled(0f)
        GameScreen.recenterCamera()
    }

    fun advanceTime(delta: Float) {
        updateTime(time + delta.toDouble())
        LevelKeeper.advanceTime(delta)
    }

    private fun updateTime(newTime: Double) {
        val monthNames = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")

        time = newTime
        val day = (time / TURNS_PER_DAY).toInt()
        val timeOfDay = time - (day * TURNS_PER_DAY)
        val minutes = (timeOfDay / TURNS_PER_DAY) * 1440.0
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
        val realYear = YEAR_ZERO + year

        timeString = "$amhour:$minstr $ampm"
        dateString = "$monthName $monthDay, $realYear"
        lastHour = hour

        LevelKeeper.forEachLiveLevel { it.updateAmbientLight(hour, minute) }
    }

    fun saveAndQuit() {
        GameScreen.addModal(
            ConfirmModal(
                listOf("Quit the game?", "Your progress will be saved."),
                "Quit", "Cancel"
            ) { yes ->
                if (yes) {
                    GameScreen.addModal(LoadingModal("Recording your deeds..."))
                    KtxAsync.launch {
                        delay(200)
                        saveStateAndExitProcess()
                    }
                } else {
                    ConsolePanel.say("You remember that one thing you needed to do...")
                }
            }
        )
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

    fun openInventory() { GameScreen.addModal(InventoryModal(player)) }
    fun openMap() { GameScreen.addModal(MapModal()) }
    fun openSystemMenu() { GameScreen.addModal(SystemMenu()) }
    fun openJournal() { }
}
