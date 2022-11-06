package audio

import RESOURCE_FILE_DIR
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.files.FileHandle
import util.Dice
import util.XY
import util.distanceBetween
import util.filterAnd
import java.lang.Double.max
import java.lang.Double.min

object Speaker {

    var volumeMaster = 1.0
    var volumeWorld = 1.0
    var volumeMusic = 1.0
    var volumeUI = 1.0

    private val maxVolumeUI = 0.7

    private val audio = Gdx.audio

    class Deck(
        val song: Music
    ) {
        var done = false
        var fader = 0.0
        var faderTarget = 1.0
        val faderSpeed = 0.4

        init {
            song.apply {
                volume = (fader * volumeMaster * volumeMusic).toFloat()
                isLooping = true
                play()
            }
        }
        fun requestFadeout() { faderTarget = 0.0 }
        fun onRender(delta: Float) {
            if (fader < faderTarget) {
                fader = min(1.0, fader + faderSpeed * delta)
            } else if (fader > faderTarget) {
                fader = max(0.0, fader - faderSpeed * delta)
                if (fader == 0.0) done = true
            }
            song.setVolume((fader * volumeMaster * volumeMusic).toFloat())
        }
        fun dispose() { song.dispose() }
    }

    private val decks = mutableListOf<Deck>()

    enum class Song(val file: String) {
        ATTRACT("finitepool.ogg"),
        WORLD("jacobsladder.ogg"),
        DUNGEON("equivalenttree.ogg")
    }

    enum class SFX(
        val files: List<String>,
        val pitchVariance: Float = 0f,
        ) {
        UIMOVE(listOf("ui/move.ogg")),
        UISELECT(listOf("ui/select.ogg")),
        UIOPEN(listOf("ui/open.ogg")),
        UICLOSE(listOf("ui/close.ogg")),

        STEPDIRT(listOf("steps/stepdirt1.ogg", "steps/stepdirt2.ogg", "steps/stepdirt3.ogg"), 0.3f),
        STEPGRASS(listOf("steps/stepgrass1.ogg", "steps/stepgrass2.ogg", "steps/stepgrass3.ogg"), 0.3f),
        STEPHARD(listOf("steps/stephard1.ogg", "steps/stephard2.ogg", "steps/stephard3.ogg"), 0.3f),

        MISS(listOf("hits/miss1.ogg", "hits/miss2.ogg"), 0.2f),
        DIG(listOf("hits/dig1.ogg", "hits/dig2.ogg"), 0.3f),
        HIT(listOf("hits/hit1.ogg", "hits/hit2.ogg"), 0.4f),

        MOO(listOf("creature/moo1.ogg", "creature/moo2.ogg"), 0.3f)
    }

    private val sfxFiles = mutableMapOf<SFX, List<Sound>>()
    private var screenWidth = 1
    private var screenCenterX = 1

    fun init() {
        loadSounds()
    }

    private fun loadSounds() {
        SFX.values().forEach { sfx ->
            sfxFiles[sfx] = mutableListOf<Sound>().apply {
                sfx.files.forEach { add(
                    audio.newSound(FileHandle("${RESOURCE_FILE_DIR}/sounds/${it}"))
                )}
            }
        }
    }

    fun ui(sfx: SFX, vol: Float = 1f, pitch: Float = 1f, screenX: Int = screenCenterX) {
        val volume = (vol * volumeMaster * volumeUI * maxVolumeUI).toFloat()
        val pan = (screenX.toFloat() / screenWidth.toFloat())
        playSFX(sfx, volume, pitch, pan)
    }

    fun world(sfx: SFX?, vol: Float = 1f, pitch: Float = 1f, source: XY) {
        if (sfx == null) return
        val distance = distanceBetween(source.x, source.y, App.player.xy.x, App.player.xy.y)
        val volume = (vol * volumeMaster * volumeWorld * (1f - (distance / 16f))).toFloat()
        if (volume > 0f) {
            playSFX(sfx, volume, pitch)
        }
    }

    fun playSFX(sfx: SFX, volume: Float, pitch: Float = 1f, pan: Float = 0.5f) {
        val rpitch = pitch + Dice.float(-sfx.pitchVariance, sfx.pitchVariance)
        sfxFiles[sfx]?.random()?.play(volume, rpitch, pan)
    }

    fun requestSong(request: Song) {
        decks.forEach { it.requestFadeout() }
        decks.filterAnd({ it.done }) { it.dispose() }

        decks.add(Deck(audio.newMusic(FileHandle("${RESOURCE_FILE_DIR}/music/${request.file}"))))
    }

    fun requestMusicFade() {
        decks.forEach { it.requestFadeout() }
    }

    fun onRender(delta: Float) {
        decks.forEach { it.onRender(delta) }
    }

    fun onResize(width: Int, height: Int) {
        this.screenWidth = width
        this.screenCenterX = width / 2
    }

    fun dispose() {
        decks.forEach { it.dispose() }
        sfxFiles.forEach { (_, files) ->
            files.forEach { it.dispose() }
        }
    }

}
