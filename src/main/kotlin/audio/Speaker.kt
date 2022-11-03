package audio

import RESOURCE_FILE_DIR
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.files.FileHandle
import util.filterAnd
import java.lang.Double.max
import java.lang.Double.min

object Speaker {

    var volumeMaster = 1.0
    var volumeWorld = 1.0
    var volumeMusic = 1.0
    var volumeUI = 1.0

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
        WORLD("jacobsladder.ogg")
    }

    fun requestSong(request: Song) {
        decks.forEach { it.requestFadeout() }
        decks.filterAnd({ it.done }) { it.dispose() }

        decks.add(Deck(audio.newMusic(FileHandle("${RESOURCE_FILE_DIR}/music/${request.file}"))))
    }


    fun requestMusicFade() {
        decks.forEach { it.requestFadeout() }
    }

    fun dispose() {
        decks.forEach { it.dispose() }
    }

    fun onRender(delta: Float) {
        decks.forEach { it.onRender(delta) }
    }
}
