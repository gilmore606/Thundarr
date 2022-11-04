package audio

import RESOURCE_FILE_DIR
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.files.FileHandle
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

    enum class SFX(val file: String) {
        UIMOVE("ui/move.ogg"),
        UISELECT("ui/select.ogg"),
        UIOPEN("ui/open.ogg"),
        UICLOSE("ui/close.ogg")
    }

    private val sfxFiles = mutableMapOf<SFX, Sound>()
    private var screenWidth = 1
    private var screenHeight = 1
    private var screenCenterX = 1

    fun init() {
        loadSounds()
    }

    private fun loadSounds() {
        SFX.values().forEach {
            sfxFiles[it] = audio.newSound(FileHandle("${RESOURCE_FILE_DIR}/sounds/${it.file}"))
        }
    }

    fun ui(sfx: SFX, vol: Float = 1f, pitch: Float = 1f, screenX: Int = screenCenterX) {
        val volume = (vol * volumeMaster * volumeUI * maxVolumeUI).toFloat()
        val pan = (screenX.toFloat() / screenWidth.toFloat())
        sfxFiles[sfx]?.play(volume, pitch, pan)
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
        this.screenHeight = height
        this.screenCenterX = width / 2
    }

    fun dispose() {
        decks.forEach { it.dispose() }
        sfxFiles.forEach { (_, file) ->
            file.dispose()
        }
    }

}
