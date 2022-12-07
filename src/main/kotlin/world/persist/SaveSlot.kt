package world.persist

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
import world.Building
import world.Chunk
import world.ChunkMeta
import world.ChunkScratch
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.util.Base64

class SaveSlot(
    private val id: String
) {
    object PrefsStateTable : Table() {
        val state = text("state")
    }

    object WorldStateTable : Table() {
        val state = text("state")
    }

    object WorldChunksTable : Table() {
        val x = integer("x")
        val y = integer("y")
        val data = text("data")
    }

    object WorldMetaTable : Table() {
        val x = integer("x")
        val y = integer("y")
        val data = text("data")
    }

    object LevelChunksTable : Table() {
        val id = varchar("id", 80)
        val building = varchar("building", 80)
        val data = text("data")
    }

    object BuildingsTable: Table() {
        val id = varchar("id", 80)
        val x = integer("x")
        val y = integer("y")
        val data = text("data")
    }

    private val saveFileFolder = "savegame"
    private val COMPRESS_ENABLED = true

    init {
        Files.createDirectories(Paths.get(saveFileFolder))
        Database.connect("jdbc:sqlite:$saveFileFolder/${id}.thundarr", "org.sqlite.JDBC")
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        transaction {
            SchemaUtils.create(PrefsStateTable, WorldStateTable, WorldMetaTable, WorldChunksTable, LevelChunksTable, BuildingsTable)
        }
    }

    fun worldExists() = transaction { WorldStateTable.selectAll().count().toInt() } > 0

    suspend fun eraseAll() {
        transaction {
            WorldChunksTable.deleteAll()
            WorldStateTable.deleteAll()
            WorldMetaTable.deleteAll()
            LevelChunksTable.deleteAll()
            BuildingsTable.deleteAll()
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

    fun hasWorldChunk(x: Int, y: Int) = transaction {
            WorldChunksTable.select {
                (WorldChunksTable.x eq x) and (WorldChunksTable.y eq y)
            }.count().toInt() } > 0

    fun getWorldChunk(x: Int, y: Int) = if (hasWorldChunk(x, y)) {
            transaction {
                WorldChunksTable.select {
                    (WorldChunksTable.x eq x) and (WorldChunksTable.y eq y)
                }.singleOrNull()?.let {
                    fromCompressed<Chunk>(it[WorldChunksTable.data])
                }
            } ?: run { throw RuntimeException("Could not load chunk at $x $y!")}
        } else throw RuntimeException("Could not find and load chunk at $x $y!")

    fun putWorldChunk(chunk: Chunk, callback: ()->Unit) {
        transaction {
            WorldChunksTable.deleteWhere { (x eq chunk.x) and (y eq chunk.y) }
            WorldChunksTable.insert {
                it[x] = chunk.x
                it[y] = chunk.y
                it[data] = toCompressed(chunk)
            }
            callback()
        }
    }

    suspend fun getWorldMeta(x: Int, y: Int) = transaction {
        WorldMetaTable.select {
            (WorldMetaTable.x eq x) and (WorldMetaTable.y eq y)
        }.singleOrNull()?.let {
            fromCompressed<ChunkMeta>(it[WorldMetaTable.data])
        }
    }

    suspend fun getWorldMetaColumn(x: Int) = transaction {
        WorldMetaTable.select {
            WorldMetaTable.x eq x
        }.map { fromCompressed<ChunkMeta>(it[WorldMetaTable.data]) }
    }

    suspend fun putWorldMetas(metas: Array<ChunkScratch>) = transaction {
        metas.forEach { meta ->
            WorldMetaTable.insert {
                it[x] = meta.x
                it[y] = meta.y
                it[data] = toCompressed(meta.toChunkMeta())
            }
        }
    }

    fun getWorldState(): WorldState {
        var result: ResultRow? = null
        var state: WorldState? = null
        transaction {
            result = WorldStateTable.selectAll().first()
        }
        result?.also {
            state = fromCompressed(it[WorldStateTable.state])
        }
        return state ?: throw RuntimeException("Could not get worldstate from db!")
    }

    fun putWorldState(state: WorldState) {
        transaction {
            WorldStateTable.deleteAll()
            WorldStateTable.insert {
                it[WorldStateTable.state] = toCompressed(state)
            }
        }
        log.info("Saved world state.")
    }

    fun getPrefsState(): PrefsState? {
        val exists = transaction { PrefsStateTable.selectAll().count().toInt() }
        if (exists < 1) return null

        var result: ResultRow? = null
        var state: PrefsState? = null
        transaction {
            result = PrefsStateTable.selectAll().first()
        }
        result?.also {
            state = fromCompressed(it[PrefsStateTable.state])
        }
        return state
    }

    fun putPrefsState(state: PrefsState) {
        transaction {
            PrefsStateTable.deleteAll()
            PrefsStateTable.insert {
                it[PrefsStateTable.state] = toCompressed(state)
            }
        }
        log.info("Saved prefs.")
    }

    fun hasLevelChunk(levelId: String) = transaction {
            LevelChunksTable.select {
                (LevelChunksTable.id eq levelId) and (LevelChunksTable.data neq "")
            }.count().toInt() } > 0

    fun getLevelChunk(levelId: String) = if (hasLevelChunk(levelId)) {
            transaction {
                LevelChunksTable.select {
                    LevelChunksTable.id eq levelId
                }.singleOrNull()?.let {
                    fromCompressed<Chunk>(it[LevelChunksTable.data])
                }
            } ?: throw RuntimeException("Could not load chunk $levelId !")
        } else throw RuntimeException("Could not find and load chunk $levelId !")

    fun putLevelChunk(chunk: Chunk, levelId: String, buildingId: String) {
        transaction {
            LevelChunksTable.deleteWhere { id eq levelId }
            LevelChunksTable.insert {
                it[id] = levelId
                it[building] = buildingId
                it[data] = toCompressed(chunk)
            }
        }
    }

    fun updateLevelChunk(chunk: Chunk, levelId: String, callback: ()->Unit) {
        transaction {
            LevelChunksTable.update({ LevelChunksTable.id eq levelId }) { it[data] = toCompressed(chunk) }
            callback()
        }
    }

    fun getBuilding(id: String) =
        transaction {
            BuildingsTable.select { BuildingsTable.id eq id }.singleOrNull()?.let {
                fromCompressed<Building>(it[BuildingsTable.data])
            }
        }

    fun getBuildingForLevel(levelId: String) = transaction {
        LevelChunksTable.select { LevelChunksTable.id eq levelId }.singleOrNull()?.let {
            it[LevelChunksTable.building]
        }
    }?.let { getBuilding(it) }

    fun putBuilding(building: Building) {
        transaction {
            BuildingsTable.deleteWhere { id eq building.id }
            BuildingsTable.insert {
                it[id] = building.id
                it[x] = building.xy.x
                it[y] = building.xy.y
                it[data] = toCompressed(building)
            }
            // Pre-create the LevelChunks row so the building id can be found for the level.
            if (!hasLevelChunk(building.firstLevelId)) {
                LevelChunksTable.insert {
                    it[id] = building.firstLevelId
                    it[LevelChunksTable.building] = building.id
                    it[data] = ""
                }
            }
        }
    }
}
