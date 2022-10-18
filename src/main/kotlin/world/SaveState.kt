package world

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import util.gzipCompress
import util.gzipDecompress
import util.log
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.util.Base64

class SaveState(
    private val id: String
) {
    object WorldChunksTable : Table() {
        val x = integer("x")
        val y = integer("y")
        val data = text("data")
    }

    object WorldStateTable : Table() {
        val state = text("state")
    }

    // TODO: figure out why we have to know the full path?
    private val saveFileFolder = "/githome/Thundarr/savegame"
    private val COMPRESS_ENABLED = true

    init {
        Files.createDirectories(Paths.get(saveFileFolder))
        Database.connect("jdbc:sqlite:$saveFileFolder/${id}.thundarr", "org.sqlite.JDBC")
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        transaction {
            SchemaUtils.create(WorldChunksTable, WorldStateTable)
        }
    }

    fun worldExists(): Boolean {
        var result = 0
        transaction {
            result = WorldStateTable.selectAll().count().toInt()
        }
        return result > 0
    }

    fun eraseAll() {
        transaction {
            WorldChunksTable.deleteAll()
            WorldStateTable.deleteAll()
        }
    }

    private inline fun <reified T: Any>toCompressed(obj: T): String {
        if (COMPRESS_ENABLED) {
            val json = Json.encodeToString<T>(obj)
            return Base64.getEncoder().encode(json.gzipCompress()).decodeToString()
        }
        return Json.encodeToString(obj)
    }

    private inline fun <reified T: Any>fromCompressed(text: String): T {
        if (COMPRESS_ENABLED) {
            val json = Base64.getDecoder().decode(text).gzipDecompress()
            return Json.decodeFromString(json)
        }
        return Json.decodeFromString(text)
    }

    private fun hasWorldChunk(x: Int, y: Int): Boolean {
        var result = 0
        transaction {
            result = WorldChunksTable.select {
                (WorldChunksTable.x eq x) and (WorldChunksTable.y eq y)
            }.count().toInt()
        }
        return result > 0
    }

    fun getWorldChunk(x: Int, y: Int, level: Level): Chunk {
        var chunk: Chunk? = null
        if (hasWorldChunk(x, y)) {
            log.debug("Loading chunk at $x $y")
            transaction {
                WorldChunksTable.select {
                    (WorldChunksTable.x eq x) and (WorldChunksTable.y eq y)
                }.singleOrNull()?.let {
                    chunk = fromCompressed(it[WorldChunksTable.data])
                }
            }
            chunk?.onRestore(level) ?: run { throw RuntimeException("Could not find chunk $x $y in db!") }
        } else {
            log.debug("Creating chunk at $x $y")
            chunk = Chunk(CHUNK_SIZE, CHUNK_SIZE).apply {
                onCreate(level, x, y, forWorld = true)
            }
        }
        return chunk ?: run { throw RuntimeException("Could not create chunk at $x $y!") }
    }


    fun putWorldChunk(chunk: Chunk) {
        transaction {
            WorldChunksTable.deleteWhere {
                (WorldChunksTable.x eq chunk.x) and (WorldChunksTable.y eq chunk.y)
            }
            WorldChunksTable.insert {
                it[x] = chunk.x
                it[y] = chunk.y
                it[data] = toCompressed(chunk)
            }
        }
    }

    fun getWorldState(): App.WorldState {
        var result: ResultRow? = null
        var state: App.WorldState? = null
        transaction {
            result = WorldStateTable.selectAll().first()
        }
        result?.also {
            state = fromCompressed(it[WorldStateTable.state])
        }
        return state ?: throw RuntimeException("Could not get worldstate from db!")
    }

    fun putWorldState(state: App.WorldState) {
        transaction {
            WorldStateTable.deleteAll()
            WorldStateTable.insert {
                it[WorldStateTable.state] = toCompressed(state)
            }
        }
        log.info("Saved world state.")
    }
}
