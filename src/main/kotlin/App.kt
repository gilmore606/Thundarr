import actors.AttractPlayer
import actors.Player
import audio.Speaker
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import kotlinx.coroutines.*
import ui.input.Keyboard
import ui.input.Mouse
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
import world.journal.GameTime
import world.journal.JournalEntry
import world.level.Level
import world.level.WorldLevel
import world.persist.LevelKeeper
import world.persist.PrefsState
import world.persist.SaveSlot
import world.persist.WorldState
import world.weather.Weather
import kotlin.system.exitProcess

const val RESOURCE_FILE_DIR = "src/main/resources/"

object App : KtxGame<com.badlogic.gdx.Screen>() {

    lateinit var player: Player
    lateinit var level: Level
    lateinit var save: SaveSlot
    var weather = Weather()

    var time: Double = 200.0
    var gameTime: GameTime = GameTime(time)
    var lastHour = -1

    private var pendingJob: Job? = null

    var DEBUG_VISIBLE = false
    var DEBUG_PANEL = false

    var attractMode = true
    var isExiting = false

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
        Speaker.init()

        save = SaveSlot("myworld")
        restorePrefs()

        addScreen(render.Screen)
        setScreen<render.Screen>()

        Gdx.input.inputProcessor = InputMultiplexer(Keyboard, Mouse)

        startAttract()
    }

    private fun startAttract() {
        Screen.panels.filterAnd({true}) { Screen.removePanel(it) }
        attractMode = true
        Screen.addPanel(Console)

        level = LevelKeeper.getLevel("attract")
        weather = Weather()
        level.setPov(60, 60)
        player = AttractPlayer()
        Screen.recenterCamera()
        movePlayerIntoLevel(70, 70)
        level.onPlayerEntered()
        updateTime(Dice.range(200, 1200).toDouble())

        Screen.brightnessTarget = 1f
        KtxAsync.launch {
            delay(500)
            Screen.addModal(AttractMenu().apply { populate() })
            player.onSpawn()
            Console.clear()
        }
    }

    private fun startGame() {
        attractMode = false
        if (Screen.topModal is AttractMenu) (Screen.topModal as AttractMenu).dismissSelf()
        TimeButtons.state = TimeButtons.State.PAUSE
    }

    private fun addGamePanels() {
        Screen.addPanel(StatusPanel)
        Screen.addPanel(LookPanel)
        Screen.addPanel(ActorPanel)
        Screen.addPanel(LeftButtons)
        Screen.addPanel(Toolbar)
        Screen.addPanel(TimeButtons)
    }

    override fun dispose() {
        log.info("Thundarr shutting down.")
        isExiting = true
        Screen.dispose()
        Speaker.dispose()
    }

    fun saveStateAndReturnToMenu() {
        Screen.panels.filterAnd({ it !is Modal }) { Screen.removePanel(it) }
        Speaker.clearMusic()
        pendingJob = KtxAsync.launch {
            LevelKeeper.hibernateAll()
            save.putWorldState(
                WorldState(
                    levelId = level.levelId(),
                    player = player,
                    time = time,
                    weather = weather,
                    consoleLines = Console.lines,
                    toolbarTags = Toolbar.getTagsForSave()
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
                volumeMaster = Speaker.volumeMaster,
                volumeWorld = Speaker.volumeWorld,
                volumeMusic = Speaker.volumeMusic,
                volumeUI = Speaker.volumeUI
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
            weather = state.weather
            updateTime(state.time)
            Console.restoreLines(state.consoleLines)
            Toolbar.loadTagsFromSave(state.toolbarTags)

            while (!level.isReady()) {
                log.info("Waiting for level...")
                delay(100)
            }
            movePlayerIntoLevel(player.xy.x, player.xy.y)
            log.info("Restored state with player at ${player.xy.x} ${player.xy.y}.")
            delay(300)
            Screen.brightnessTarget = 1f
            addGamePanels()
            level.onPlayerEntered()
            updateTime(time)

            repeat(5) { Bomb().moveTo(player) }
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

                Speaker.volumeMaster = state.volumeMaster
                Speaker.volumeWorld = state.volumeWorld
                Speaker.volumeMusic = state.volumeMusic
                Speaker.volumeUI = state.volumeUI
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
            player = Player()
            player.onSpawn()

            level.setPov(200, 200)

            var playerStart: XY? = null
            while (playerStart == null) {
                log.debug("Waiting for chunks...")
                delay(200)
                playerStart = level.getPlayerEntranceFrom(level.levelId())
            }
            delay(500)
            movePlayerIntoLevel(playerStart.x, playerStart.y)
            Console.clear()
            Console.say("You stride bravely into the dawn.")
            Screen.brightnessTarget = 1f
            addGamePanels()
            level.onPlayerEntered()
            updateTime(Dice.range(200, 600).toDouble())
            player.journal.addEntry(JournalEntry(
                "Freedom!",
                "I broke the chains of enslavement to the evil wizard Madlibizus, and set out to find my destiny.  May the Lords of Light guide my Sunsword, and my path."
            ))
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
            level.onPlayerEntered()
            updateTime(time)
        }
    }

    fun enterWorldFromLevel(dest: XY) {
        level = LevelKeeper.getWarmedWorld(dest)
        Speaker.requestSong(Speaker.Song.WORLD)

        KtxAsync.launch {
            while (!level.isReady()) {
                log.debug("Waiting for world...")
                delay(50)
            }
            movePlayerIntoLevel(dest.x, dest.y)
            Console.say("You step back outside to face the wilderness once again.")
            level.onPlayerEntered()
            updateTime(time)
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
        time = newTime
        gameTime = GameTime(time)
        lastHour = gameTime.hour

        weather.updateTime(gameTime.hour, gameTime.minute, level)
        LevelKeeper.forEachLiveLevel { it.updateTime(gameTime.hour, gameTime.minute) }
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
