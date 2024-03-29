package audio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ktx.async.KtxAsync
import ui.panels.TimeButtons
import util.*
import java.lang.Double.max
import java.lang.Double.min
import java.lang.RuntimeException
import java.util.Collections.addAll

object Speaker {

    var volumeMaster = 1.0
    var volumeWorld = 1.0
    var volumeMusic = 0.6
    var volumeUI = 0.6

    val gainMaster = 1.0
    val gainWorld = 1.0
    val gainMusic = 1.0
    val gainUI = 0.5

    @Serializable
    enum class Song(
        val file: String
        ) {
        ATTRACT("yeshallnot.ogg"),
        WORLD("jacobsladder.ogg"),
        DUNGEON("equivalenttree.ogg")
    }

    @Serializable
    enum class SFX(
        val files: List<String>,
        val pitchVariance: Float = 0f,
        val gain: Float = 1f
    ) {
        UIMOVE(listOf("ui/move.ogg")),
        UISELECT(listOf("ui/select.ogg")),
        UIOPEN(listOf("ui/open.ogg")),
        UICLOSE(listOf("ui/close.ogg")),
        UIERROR(listOf("ui/error.ogg")),
        UIAWARD(listOf("ui/award.ogg")),

        STEPDIRT(listOf("steps/stepdirt1.ogg", "steps/stepdirt2.ogg", "steps/stepdirt3.ogg"), 0.3f),
        STEPGRASS(listOf("steps/stepgrass1.ogg", "steps/stepgrass2.ogg", "steps/stepgrass3.ogg"), 0.3f, 0.7f),
        STEPHARD(listOf("steps/stephard1.ogg", "steps/stephard2.ogg", "steps/stephard3.ogg"), 0.3f),
        STEPWATER(listOf("steps/stepwater1.ogg", "steps/stepwater2.ogg", "steps/stepwater3.ogg"), 0.3f),

        THUNDER_NEAR(listOf("weather/thundernear.ogg"), 0.4f),
        THUNDER_DISTANT(listOf("weather/thunderdistant.ogg"), 0.2f),

        MISS(listOf("hits/miss1.ogg", "hits/miss2.ogg"), 0.2f, 0.5f),
        DIG(listOf("hits/dig1.ogg", "hits/dig2.ogg"), 0.3f),
        HIT(listOf("hits/hit1.ogg", "hits/hit2.ogg"), 0.4f, 1.5f),
        BRICKHIT(listOf("hits/brickhit.ogg"), 0.3f),
        ROCKHIT(listOf("hits/rockhit.ogg"), 0.3f),
        EXPLODE(listOf("hits/explode.ogg"), 0.1f),
        TREEFALL(listOf("misc/treefall.ogg"), 0.1f, 0.8f),
        DOOR_CLOSE(listOf("misc/doorclose.ogg"), 0.2f, 0.6f),
        DOOR_OPEN(listOf("misc/dooropen.ogg"), 0.2f),
        PRAYER(listOf("misc/prayer.ogg"), 0f),

        MOO(listOf("creature/moo1.ogg", "creature/moo2.ogg"), 0.4f),
        VOICE_MALEHIGH(listOf("voice/malehigh1.ogg", "voice/malehigh2.ogg"), 0.06f, 0.8f),
        VOICE_MALELOW(listOf("voice/malelow1.ogg"), 0.1f),
        VOICE_FEMALE(listOf("voice/female1.ogg", "voice/female2.ogg"), 0.06f),
        RAT(listOf("creature/ratsqueak.ogg"), 0.3f)
    }

    @Serializable
    enum class Ambience(
        val file: String
    ) {
        RAINLIGHT("weather/rainlight.ogg"),
        RAINHEAVY("weather/rainheavy.ogg"),
        INDUSTRIAL("ambi/industrial.ogg"),
        OUTDOORDAY("ambi/outdoorday.ogg"),
        OUTDOORNIGHT("ambi/outdoornight.ogg"),
        DESERT("ambi/desert_day.ogg"),
        FOREST("ambi/forest.ogg"),
        MOUNTAIN("ambi/mountain.ogg"),
        RUINS("ambi/ruins.ogg"),
        SWAMP("ambi/swamp.ogg"),
        OCEAN("ambi/ocean.ogg"),
        RIVER1("ambi/river1.ogg"),
        RIVER2("ambi/river2.ogg"),
        RIVER3("ambi/river3.ogg"),
        LAVA("ambi/lava.ogg"),
        SHRINECHORD("ambi/shrine.ogg")
    }

    @Serializable
    class PointAmbience(
        val ambience: Ambience,
        val falloff: Float = 40f,
        val volume: Float = 1f
    )


    private val audio = Gdx.audio

    class Deck(
        val song: Music,
        val isMusic: Boolean = true
    ) {
        var doneAtZero = false
        var done = false
        var fader = 0.0
        var faderTarget = if (isMusic) 1.0 else 0.0
        val faderSpeed = 0.4

        init {
            song.apply {
                volume = (fader * localMaster()).toFloat()
                isLooping = true
                play()
            }
        }
        fun requestVolume(newVolume: Float) {
            faderTarget = newVolume * localMaster()
            doneAtZero = false
            done = false
        }
        fun requestFadeout() {
            faderTarget = 0.0
            doneAtZero = true
        }
        fun onRender(delta: Float) {
            if (fader < faderTarget) {
                fader = min(1.0, fader + faderSpeed * delta)
            } else if (fader > faderTarget) {
                fader = max(0.0, fader - faderSpeed * delta)
            }
            if (fader <= 0.0 && doneAtZero) done = true
            song.volume = (fader * localMaster()).toFloat()
        }
        fun localMaster() = (if (isMusic) volumeMusic * gainMusic else volumeWorld * gainWorld) * volumeMaster * gainMaster
        fun dispose() { song.dispose() }
        fun abortDone() { done = false ; doneAtZero = false }
    }

    private val musicDecks = mutableListOf<Deck>()
    private val ambiDecks = mutableMapOf<Ambience, Deck>()
    private val sfxFiles = mutableMapOf<SFX, List<Sound>>()

    class QueuedSound(val sound: Sound, val volume: Float, val pitch: Float, val pan: Float)

    private val sfxQueue = ArrayList<QueuedSound>()
    private val sfxQueueMax = 4
    private val sfxMinDelay = 0.04f
    private var sfxDelay = 0f

    private var screenWidth = 1
    private var screenCenterX = 1

    fun init() {
        loadSounds()
    }

    private fun loadSounds() {
        SFX.values().forEach { sfx ->
            sfxFiles[sfx] = mutableListOf<Sound>().apply {
                sfx.files.forEach { add(
                    audio.newSound(Gdx.files.internal("res/sounds/${it}"))
                )}
            }
        }
    }

    fun ui(sfx: SFX, vol: Float = 1f, pitch: Float = 1f, screenX: Int = screenCenterX) {
        val volume = (vol * volumeMaster * gainMaster * volumeUI * gainUI * sfx.gain).toFloat()
        val pan = (screenX.toFloat() / screenWidth.toFloat())
        playSFX(sfx, volume, pitch, pan)
    }

    fun world(sfx: SFX?, vol: Float = 1f, pitch: Float = 1f, source: XY? = null, delayMs: Long = 0L) {
        if (sfx == null) return
        val distance = source?.let { distanceBetween(it.x, it.y, App.player.xy.x, App.player.xy.y) } ?: 0f
        val volume = (vol * volumeMaster * gainMaster * volumeWorld * gainWorld * sfx.gain * (1f - (distance / 20f))).toFloat()
        if (volume > 0f) {
            if (TimeButtons.state != TimeButtons.State.FFWD) {
                if (delayMs > 0L) {
                    KtxAsync.launch {
                        delay(delayMs)
                        playSFX(sfx, volume, pitch)
                    }
                } else playSFX(sfx, volume, pitch)
            }
        }
    }

    private fun playSFX(sfx: SFX, volume: Float, pitch: Float = 1f, pan: Float = 0.5f) {
        if (sfxQueue.size >= sfxQueueMax) return
        val rpitch = pitch + Dice.float(-sfx.pitchVariance, sfx.pitchVariance)
        sfxFiles[sfx]?.also { files ->
            sfxQueue.add(QueuedSound(files.random(), volume, rpitch, pan))
        }
    }

    private fun cleanDecks() {
        musicDecks.filterAnd({ it.done }) { it.dispose() }
        val ambiences = ArrayList<Ambience>().apply { addAll(ambiDecks.keys) }
        ambiences.forEach { ambience ->
            if (ambiDecks[ambience]?.done == true) {
                ambiDecks[ambience]?.dispose()
                ambiDecks.remove(ambience)
            }
        }
    }

    fun requestSong(request: Song) {
        musicDecks.forEach { it.requestFadeout() }

        musicDecks.add(Deck(audio.newMusic(Gdx.files.internal("res/sounds/music/${request.file}"))))
    }

    fun clearMusic() {
        musicDecks.forEach { it.requestFadeout() }
    }

    fun requestAmbience(request: Ambience) {
        if (request !in ambiDecks.keys) {
            ambiDecks[request] = Deck(audio.newMusic(Gdx.files.internal("res/sounds/${request.file}")), false)
        } else {
            ambiDecks[request]?.abortDone()
        }
    }

    fun adjustAmbience(ambience: Ambience, volume: Float) {
        ambiDecks[ambience]?.also { deck ->
            deck.requestVolume(volume)
        }
    }

    fun clearAmbience() {
        ambiDecks.values.forEach {
            it.requestFadeout()
        }
    }

    fun setAmbiences(ambiences: List<Pair<Ambience, Float>>) {
        val unplayed = ambiences.toMutableList()
        ambiDecks.keys.forEach { currentTrack ->
            var wanted = false
            var removeAt = -1
            unplayed.forEachIndexed { n, unplayedTrack ->
                if (unplayedTrack.first == currentTrack) {
                    wanted = true
                    removeAt = n
                    adjustAmbience(currentTrack, unplayedTrack.second)
                }
            }
            if (!wanted) {
                ambiDecks[currentTrack]?.requestFadeout()
            } else {
                unplayed.removeAt(removeAt)
            }
        }
        unplayed.forEach { unplayedTrack ->
            log.info("adding new ambience ${unplayedTrack.first} ${unplayedTrack.second}")
            requestAmbience(unplayedTrack.first)
            adjustAmbience(unplayedTrack.first, unplayedTrack.second)
        }
        cleanDecks()
    }

    fun onRender(delta: Float) {
        musicDecks.forEach { it.onRender(delta) }
        ambiDecks.values.forEach { it.onRender(delta) }
        sfxDelay += delta
        if (sfxDelay > sfxMinDelay && sfxQueue.isNotEmpty()) {
            sfxDelay = 0f
            val sfx = sfxQueue.removeAt(0)
            sfx.sound.play(sfx.volume, sfx.pitch, sfx.pan)
        }
    }

    fun onResize(width: Int, height: Int) {
        this.screenWidth = width
        this.screenCenterX = width / 2
    }

    fun dispose() {
        musicDecks.forEach { it.dispose() }
        ambiDecks.values.forEach { it.dispose() }
        sfxFiles.forEach { (_, files) ->
            files.forEach { it.dispose() }
        }
    }

}
