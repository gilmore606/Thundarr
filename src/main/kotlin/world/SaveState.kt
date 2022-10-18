package world

import App.saveFileFolder
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import util.XY
import util.gzipCompress
import util.gzipDecompress
import util.log
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class SaveState(
    private val id: String
) {
    private val json = Json { }
    init {
        Files.createDirectories(Paths.get(filepath()))
    }

    fun worldExists(): Boolean = File(filepath() + "world.json.gz").exists()

    fun eraseAll() {
        File(filepath()).listFiles()?.forEach { file ->
            file.delete()
        }
    }

    fun getWorldChunk(xy: XY, level: Level): Chunk {
        val filename = chunkpath(xy.x, xy.y)
        return if (File(filename).exists()) {
            log.debug("Loading chunk at $xy")
            val chunk = Json.decodeFromString<Chunk>(File(filename).readBytes().gzipDecompress())
            chunk.onRestore(level)
            chunk
        } else {
            log.debug("Creating chunk at $xy")
            val chunk = Chunk(CHUNK_SIZE, CHUNK_SIZE)
            chunk.onCreate(level, xy.x, xy.y, forWorld = true)
            chunk
        }
    }

    fun putWorldChunk(chunk: Chunk) {
        File(chunkpath(chunk.x, chunk.y)).writeBytes(json.encodeToString(chunk).gzipCompress())
    }

    fun getWorldState(): App.WorldState =
        Json.decodeFromString<App.WorldState>(File(filepath() + "world.json.gz").readBytes().gzipDecompress())

    fun putWorldState(state: App.WorldState) {
        File(filepath() + "world.json.gz").writeBytes(json.encodeToString(state).gzipCompress())
        log.info("Saved world state.")
    }

    private fun filepath() = "$saveFileFolder/$id/"
    private fun chunkpath(x: Int, y: Int) = filepath() + "chunk" + x.toString() + "x" + y.toString() + ".json.gz"
}
