import actors.Player
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import kotlinx.coroutines.*
import ui.input.Keyboard
import ui.input.Mouse
import kotlinx.serialization.Serializable
import ktx.app.KtxGame
import ktx.async.KTX
import ktx.async.KtxAsync
import render.Screen
import things.*
import ui.modals.*
import ui.panels.*
import util.XY
import util.log
import world.*
import kotlin.system.exitProcess

const val RESOURCE_FILE_DIR = "src/main/resources/"

object App : KtxGame<com.badlogic.gdx.Screen>() {

    @Serializable
    data class WorldState(
        val levelId: String,
        val player: Player,
        val time: Double,
        val zoomIndex: Double,
        val windowSize: XY,
        val fullscreen: Boolean,
        val worldZoom: Double,
        val cameraSlack: Double,
        val cameraMenuShift: Double
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

        addScreen(render.Screen)
        setScreen<render.Screen>()
        Screen.addPanel(Console)
        Screen.addPanel(StatusPanel)
        Screen.addPanel(LookPanel)
        Screen.addPanel(ActorPanel)
        Screen.addPanel(LeftButtons)

        Gdx.input.inputProcessor = InputMultiplexer(Keyboard, Mouse)

        Console.say("The moon is broken in god-damned half!")
        Console.say("\"Demon dogs!\"")
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
        Screen.dispose()
    }

    private fun saveStateAndExitProcess() {
        pendingJob = KtxAsync.launch {
            LevelKeeper.hibernateAll()

            save.putWorldState(
                WorldState(
                    levelId = level.levelId(),
                    player = player,
                    time = time,
                    zoomIndex = Screen.zoomIndex,
                    windowSize = Screen.savedWindowSize(),
                    fullscreen = Screen.FULLSCREEN,
                    cameraSlack = Screen.cameraSlack,
                    cameraMenuShift = Screen.cameraMenuShift,
                    worldZoom = Screen.worldZoom
                )
            )

            log.info("State saved.")
            dispose()
            exitProcess(0)
        }
    }

    private fun restoreState() {
        Screen.addModal(LoadingModal("Returning to the wasteland..."))
        pendingJob = KtxAsync.launch {
            LevelKeeper.hibernateAll()

            val state = save.getWorldState()

            Screen.resize(state.windowSize.x, state.windowSize.y)
            if (state.fullscreen) {
                Screen.toggleFullscreen(true)
            } else {
                Gdx.graphics.setWindowedMode(state.windowSize.x, state.windowSize.y)
            }

            level = LevelKeeper.getLevel(state.levelId)
            if (level !is WorldLevel) LevelKeeper.getLevel("world")

            player = state.player
            player.onRestore()
            level.setPov(player.xy.x, player.xy.y)

            Screen.restoreZoomIndex(state.zoomIndex)
            Screen.worldZoom = state.worldZoom
            Screen.cameraSlack = state.cameraSlack
            Screen.cameraMenuShift = state.cameraMenuShift

            updateTime(state.time)

            while (!level.isReady()) {
                log.info("Waiting for level...")
                delay(100)
            }
            movePlayerIntoLevel(player.xy.x, player.xy.y)
            log.info("Restored state with player at ${player.xy.x} ${player.xy.y}.")
        }
    }

    private fun createNewWorld() {
        Screen.addModal(LoadingModal("The moon...it's breaking in half!"))
        pendingJob = KtxAsync.launch {
            LevelKeeper.hibernateAll()
            save.eraseAll()

            level = LevelKeeper.getLevel("world")
            player = Player()
            Sunsword().moveTo(player)
            Torch().moveTo(player)
            Torch().moveTo(player)
            Apple().moveTo(player)
            HornetHelmet().moveTo(player)
            HardHat().moveTo(player)
            RiotHelmet().moveTo(player)
            Pickaxe().moveTo(player)
            Axe().moveTo(player)

            updateTime(0.0)
            level.setPov(200, 200)

            var playerStart: XY? = null
            while (playerStart == null) {
                log.debug("Waiting for chunks...")
                delay(50)
                playerStart = level.getPlayerEntranceFrom(level.levelId())
            }
            movePlayerIntoLevel(playerStart.x, playerStart.y)
            Console.say("You step tentatively into the apocalypse...")
        }
    }

    fun enterLevelFromWorld(levelId: String) {
        val oldLevelId = level.levelId()
        level = LevelKeeper.getLevel(levelId)
        KtxAsync.launch {
            while (!level.isReady()) {
                log.debug("Waiting for level...")
                delay(50)
            }
            val entrance = level.getPlayerEntranceFrom(oldLevelId)
            movePlayerIntoLevel(entrance!!.x, entrance!!.y)
            Console.say("You cautiously step inside...")
        }
    }

    fun enterWorldFromLevel(dest: XY) {
        level = LevelKeeper.getWarmedWorld(dest)

        KtxAsync.launch {
            while (!level.isReady()) {
                log.debug("Waiting for world...")
                delay(50)
            }
            movePlayerIntoLevel(dest.x, dest.y)
            Console.say("You step back outside to face the wilderness once again.")
        }
    }

    private fun movePlayerIntoLevel(x: Int, y: Int) {
        player.moveTo(level, x, y)
        updateTime(time)
        level.onRestore()
        Screen.mouseScrolled(0f)
        Screen.recenterCamera()
    }

    fun updateTime(newTime: Double) {
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
        Screen.addModal(
            ConfirmModal(
                listOf("Quit the game?", "Your progress will be saved."),
                "Quit", "Cancel"
            ) { yes ->
                if (yes) {
                    Screen.addModal(LoadingModal("Recording your deeds..."))
                    KtxAsync.launch {
                        delay(200)
                        saveStateAndExitProcess()
                    }
                } else {
                    Console.say("You remember that one thing you needed to do...")
                }
            }
        )
    }

    fun restartWorld() {
        Screen.addModal(ConfirmModal(
            listOf(
                "Abandon this world?",
                "All your progress will be lost."),
            "Abandon", "Cancel"
        ) { yes ->
            if (yes) {
                Console.say("You abandon the world.")
                pendingJob?.cancel()
                createNewWorld()
            } else {
                Console.say("You gather your resolve and carry on.")
            }
        }
        )
    }

    fun openSettings() { Screen.addModal(SettingsModal()) }
    fun openControls() { Screen.addModal(ControlsModal()) }
    fun openCredits() { Screen.addModal(CreditsModal()) }
    fun openInventory(withContainer: Container? = null) { Screen.addModal(InventoryModal(player, withContainer)) }
    fun openGear() { Screen.addModal(GearModal(player)) }
    fun openMap() { Screen.addModal(MapModal()) }
    fun openSystemMenu() { Screen.addModal(SystemMenu()) }
    fun openJournal() { Screen.addModal(JournalModal()) }
}
