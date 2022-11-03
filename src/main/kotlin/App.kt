import actors.AttractPlayer
import actors.Player
import actors.stats.Brains
import actors.stats.Strength
import actors.stats.skills.*
import audio.Speaker
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
import util.Dice
import util.XY
import util.filterAnd
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
        val consoleLines: List<String>
    )

    @Serializable
    data class PrefsState(
        val zoomIndex: Double,
        val windowSize: XY,
        val fullscreen: Boolean,
        val worldZoom: Double,
        val cameraSlack: Double,
        val cameraMenuShift: Double,
        val uiHue: Double
    )

    private const val TURNS_PER_DAY = 2000.0
    private const val YEAR_ZERO = 2994

    lateinit var player: Player
    lateinit var level: Level
    lateinit var save: SaveState

    var time: Double = 200.0
    var lastHour = -1
    var timeString: String = "???"
    var dateString: String = "???"

    private var pendingJob: Job? = null

    var DEBUG_VISIBLE = false
    var DEBUG_PANEL = false

    var attractMode = true

    private fun setupLog() {
        Dispatchers.KTX.mainThread.name = "main"
        System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO")
        System.setProperty(org.slf4j.simple.SimpleLogger.SHOW_DATE_TIME_KEY, "true")
        System.setProperty(org.slf4j.simple.SimpleLogger.DATE_TIME_FORMAT_KEY, "hh:mm:ss.SSS")
        System.setProperty(org.slf4j.simple.SimpleLogger.SHOW_LOG_NAME_KEY, "false")
        System.setProperty(org.slf4j.simple.SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "false")
        log.info("Thundarr starting up!")
    }

    override fun create() {
        KtxAsync.initiate()
        setupLog()

        save = SaveState("myworld")
        restorePrefs()

        addScreen(render.Screen)
        setScreen<render.Screen>()

        Gdx.input.inputProcessor = InputMultiplexer(Keyboard, Mouse)

        startAttract()
    }

    private fun startAttract() {
        Screen.panels.filterAnd({true}) { Screen.removePanel(it) }
        attractMode = true
        Speaker.requestSong(Speaker.Song.ATTRACT)
        Screen.addPanel(TimeButtons)

        level = LevelKeeper.getLevel("attract")
        player = AttractPlayer()

        updateTime(Dice.range(700, 1200).toDouble())
        level.setPov(60, 60)
        Screen.recenterCamera()
        movePlayerIntoLevel(70, 70)

        TimeButtons.state = TimeButtons.State.PLAY
        Screen.brightnessTarget = 1f
        KtxAsync.launch {
            delay(500)
            Screen.addModal(AttractMenu().apply { populate() })
            Lightbulb().moveTo(player)
        }
    }

    private fun startGame() {
        attractMode = false
        if (Screen.topModal is AttractMenu) (Screen.topModal as AttractMenu).dismissSelf()
        TimeButtons.state = TimeButtons.State.PAUSE
    }

    private fun addGamePanels() {
        Screen.addPanel(Console)
        Screen.addPanel(StatusPanel)
        Screen.addPanel(LookPanel)
        Screen.addPanel(ActorPanel)
        Screen.addPanel(LeftButtons)
    }

    override fun dispose() {
        log.info("Thundarr shutting down.")
        Screen.dispose()
        Speaker.dispose()
    }

    private fun saveStateAndReturnToMenu() {
        Screen.panels.filterAnd({ it !is Modal }) { Screen.removePanel(it) }
        Speaker.requestMusicFade()
        pendingJob = KtxAsync.launch {
            LevelKeeper.hibernateAll()
            save.putWorldState(
                WorldState(
                    levelId = level.levelId(),
                    player = player,
                    time = time,
                    consoleLines = Console.lines
                )
            )
            delay(500)

            log.info("State saved.")
            startAttract()
        }
    }

    private fun savePrefs() {
        save.putPrefsState(
            PrefsState(
                zoomIndex = Screen.zoomIndex,
                windowSize = Screen.savedWindowSize(),
                fullscreen = Screen.FULLSCREEN,
                cameraSlack = Screen.cameraSlack,
                cameraMenuShift = Screen.cameraMenuShift,
                worldZoom = Screen.worldZoom,
                uiHue = Screen.uiHue,
            )
        )
    }

    private fun restoreState() {
        Screen.addModal(LoadingModal("Returning to Numeria..."))

        pendingJob = KtxAsync.launch {
            Screen.brightnessTarget = 0f
            LevelKeeper.hibernateAll()

            val state = save.getWorldState()



            level = LevelKeeper.getLevel(state.levelId)
            if (level !is WorldLevel) LevelKeeper.getLevel("world")

            player = state.player
            player.onRestore()
            level.setPov(player.xy.x, player.xy.y)


            Console.restoreLines(state.consoleLines)

            updateTime(state.time)

            while (!level.isReady()) {
                log.info("Waiting for level...")
                delay(100)
            }
            movePlayerIntoLevel(player.xy.x, player.xy.y)
            log.info("Restored state with player at ${player.xy.x} ${player.xy.y}.")
            delay(300)
            Screen.brightnessTarget = 1f
            addGamePanels()
            Speaker.requestSong(Speaker.Song.WORLD)
        }
    }

    private fun restorePrefs() {
        KtxAsync.launch {
            save.getPrefsState()?.also { state ->
                Screen.resize(state.windowSize.x, state.windowSize.y)
                if (state.fullscreen) {
                    Screen.toggleFullscreen(true)
                } else {
                    Gdx.graphics.setWindowedMode(state.windowSize.x, state.windowSize.y)
                }

                Screen.restoreZoomIndex(state.zoomIndex)
                Screen.worldZoom = state.worldZoom
                Screen.cameraSlack = state.cameraSlack
                Screen.cameraMenuShift = state.cameraMenuShift
                Screen.uiHue = state.uiHue
            }
        }
    }

    private fun createNewWorld() {
        Screen.addModal(LoadingModal("The moon...it's breaking in god-damned half!"))
        Screen.brightnessTarget = 0f
        pendingJob = KtxAsync.launch {
            LevelKeeper.hibernateAll()
            save.eraseAll()

            delay(400)

            level = LevelKeeper.getLevel("world")
            player = Player().apply {
                Strength.set(this, 14f)
                Brains.set(this, 9f)
                Dig.set(this, 2f)
                Fight.set(this, 1f)
                Throw.set(this, 4f)
                Build.set(this, 1f)
                Survive.set(this, 2f)
                repeat(5) { Throw.improve(this) }
            }
            Sunsword().moveTo(player)
            repeat (20) { EnergyDrink().moveTo(player) }
            Torch().moveTo(player)
            Apple().moveTo(player)
            HornetHelmet().moveTo(player)
            HardHat().moveTo(player)
            RiotHelmet().moveTo(player)
            Pickaxe().moveTo(player)
            Axe().moveTo(player)

            updateTime(Dice.range(200, 600).toDouble())
            level.setPov(200, 200)

            var playerStart: XY? = null
            while (playerStart == null) {
                log.debug("Waiting for chunks...")
                delay(200)
                playerStart = level.getPlayerEntranceFrom(level.levelId())
            }
            delay(500)
            movePlayerIntoLevel(playerStart.x, playerStart.y)
            Console.say("You stride bravely into the dawn.")
            Screen.brightnessTarget = 1f
            addGamePanels()
            Speaker.requestSong(Speaker.Song.WORLD)
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

        LevelKeeper.forEachLiveLevel { it.updateTime(hour, minute) }
    }

    fun saveAndReturnToMenu() {
        Screen.addModal(
            ConfirmModal(
                listOf("Exit the world?", "Your progress will be saved."),
                "Exit", "Cancel"
            ) { yes ->
                if (yes) {
                    Screen.brightnessTarget = 0f
                    Screen.addModal(LoadingModal("Recording your deeds..."))
                    KtxAsync.launch {
                        delay(600)
                        saveStateAndReturnToMenu()
                    }
                } else {
                    Console.say("You remember that one thing you needed to do...")
                }
            }
        )
    }

    private fun restartWorld() {
        Screen.addModal(ConfirmModal(
            listOf(
                "Abandon this world?",
                "All your progress will be lost."),
            "Abandon", "Cancel"
        ) { yes ->
            if (yes) {
                Console.say("You abandon Numeria to its own devices.")
                pendingJob?.cancel()
                startGame()
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
    fun openSkills() { Screen.addModal(SkillsModal(player)) }
    fun openMap() { Screen.addModal(MapModal()) }
    fun openSystemMenu() { Screen.addModal(SystemMenu()) }
    fun openJournal() { Screen.addModal(JournalModal()) }

    fun doContinue() {
        startGame()
        restoreState()
    }

    fun doStartNewGame() {
        if (save.worldExists()) {
            restartWorld()
        } else {
            pendingJob?.cancel()
            startGame()
            createNewWorld()
        }
    }

    fun doQuit() {
        savePrefs()
        dispose()
        exitProcess(0)
    }
}
