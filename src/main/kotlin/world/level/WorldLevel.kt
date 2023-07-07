package world.level

import audio.Speaker
import ui.panels.Console
import util.*
import world.Chunk
import world.gen.Metamap
import world.persist.ChunkLoader
import java.lang.Float.max
import java.lang.Float.min
import java.lang.Math.abs

const val CHUNK_SIZE = 64
const val CHUNKS_AHEAD = 3

class WorldLevel() : Level() {

    companion object {
        private const val chunksWide = CHUNKS_AHEAD * 2 + 1
        private const val chunkTransitionSlop = 4  // how many cells into a chunk do we go before transition?
    }

    private val dayTemperatures = listOf(
        -11, -12, -13, -14, -15, -13, -10, -8, -5, 0, 3, 6,
        8, 11, 13, 15, 12, 10, 7, 3, 0, -2, -5, -9
    )

    var needStarterDungeon = false

    private val chunks = Array(chunksWide) { Array<Chunk?>(chunksWide) { null } }
    private val loadedChunks = mutableSetOf<Chunk>()
    private var originChunkX = 0   // upper-left corner of the upper-left corner chunk
    private var originChunkY = 0

    private val lastPovChunkXY = XY(-999,  -999)  // upper-left corner of the last chunk POV was in, to check chunk crossings

    private var playerChunk: Chunk? = null   // chunk the player is currently in
    private var ambienceChunk: Chunk? = null   // chunk the player has advanced into, for weather/ambient effects.  Changes later than playerChunk!
    private var ambienceChunkXY = XY(-999,-999)

    override fun allChunks() = loadedChunks
    override fun levelId() = "world"

    override fun isReady(): Boolean {
        return loadedChunks.size >= chunksWide * chunksWide
    }

    override fun getNewPlayerEntranceFrom(): XY? {
        // Brand new start.
        var entrance = Metamap.suggestedPlayerStart
        var tries = 0
        while (tries < 200) {
            tries++
            val dx = entrance.x + Dice.zeroTo(62)
            val dy = entrance.y + Dice.zeroTo(62)
            if (isWalkableAt(App.player, dx, dy)) {
                entrance = XY(dx,dy)
                return entrance
            }
        }
        return entrance
    }

    override fun getPlayerEntranceFrom(fromLevelId: String): XY? {
        return null
    }

    override fun onPlayerEntered() {
        Speaker.clearMusic()
        Speaker.requestSong(Speaker.Song.WORLD)
        Speaker.clearAmbience()
        updateAmbientSound()
    }

    override fun updateAmbientSound() {
        val outdoors = max(0f, 1f - (distanceFromOutdoors * 0.06f))
        val day = max(0f, (ambientLight.brightness() - 0.5f) * 2f)
        val rain1 = min(1f, App.weather.rain() * 3f)
        val rain2 = max(0f, (App.weather.rain() - 0.5f) * 2f)

        val ambiences = ArrayList<Pair<Speaker.Ambience, Float>>()
        ambienceChunk?.also { chunk ->
            val meta = Metamap.metaAtWorld(App.player.xy.x, App.player.xy.y)
            log.debug("Updating ambience for chunk $chunk with biome ${meta.biome}")
            if (day > 0.5f) {
                ambiences.add(Pair(meta.biome.ambientSoundDay(), 1f * outdoors))
            } else {
                ambiences.add(Pair(meta.biome.ambientSoundNight(), 1f * outdoors))
            }
        }
        if (rain1 > 0.2f) {
            ambiences.add(Pair(Speaker.Ambience.RAINLIGHT, rain1 * outdoors))
        }
        if (rain2 > 0.2f) {
            ambiences.add(Pair(Speaker.Ambience.RAINHEAVY, rain2 * outdoors))
        }
        pointAmbienceCache.forEach { (ambience, volume) ->
            ambiences.add(Pair(ambience, volume))
        }
        Speaker.setAmbiences(ambiences)
    }

    override fun debugText(): String = chunks[CHUNKS_AHEAD][CHUNKS_AHEAD]?.let { "chunk ${it.x}x${it.y}" } ?: "???"
    override fun statusText(): String = Metamap.metaAtWorld(App.player.xy.x, App.player.xy.y).title
    override fun timeScale() = 2.0f

    // TODO: take season, fires into account
    override fun temperatureAt(xy: XY): Int {
        val meta = Metamap.metaAtWorld(xy.x, xy.y)
        val base = meta.temperature + meta.biome.temperatureBase() + App.weather.temperature()
        val daytime = (dayTemperatures[App.gameTime.hour].toFloat() * meta.biome.temperatureAmplitude()).toInt()
        var t = base + daytime
        meta.features().forEach { t += it.temperatureMod() }
        if (t > 100) {
            t = 100 + ((t - 100).toFloat() * 0.6f).toInt()
        }
        val roof = roofedAt(xy.x, xy.y)
        if (roof != Chunk.Roofed.OUTDOOR) {
            val mod = if (roof == Chunk.Roofed.WINDOW) 0.8f else 0.5f
            t = 68 + ((t - 68f) * mod).toInt()
        }
        return t
    }

    override fun onSetPov() {
        populateChunks()
        // Have we moved far enough into a new chunk for transition?
        val chunkX = xToChunkX(pov.x)
        val chunkY = yToChunkY(pov.y)
        chunkAt(pov.x, pov.y)?.also { chunk ->
            playerChunk = chunk
        } ?: run {
            playerChunk = null
        }
        val slopx = abs(pov.x - chunkX)
        val slopy = abs(pov.y - chunkY)
        if (slopx >= chunkTransitionSlop && slopx <= (CHUNK_SIZE - chunkTransitionSlop) && slopy >= chunkTransitionSlop && slopy <= (CHUNK_SIZE - chunkTransitionSlop)) {
            // If we have the current chunk do the transition
            ambienceChunkXY.x = chunkX
            ambienceChunkXY.y = chunkY
            chunkAt(pov.x, pov.y)?.also { chunk ->
                crossChunks(chunk)
            } ?: run {
                ambienceChunk = null
            }
        }
    }

    private fun crossChunks(newChunk: Chunk) {
        transitionAmbienceToChunk(newChunk)

        val threat = App.player.threatLevel()
        if (threat > 0 && App.player.lastChunkThreatLevel <= 0) {
            Console.say("You feel uneasy.")
        } else if (threat > 0 && threat > App.player.lastChunkThreatLevel) {
            Console.say("Your anxiety grows stronger.")
        } else if (threat >= 0 && threat < App.player.lastChunkThreatLevel) {
            Console.say("You feel less anxious.")
        } else if (threat <= 0 && App.player.lastChunkThreatLevel > 0) {
            Console.say("A sense of relief washes over you.")
        }
        App.player.lastChunkThreatLevel = threat
    }

    // Save all live chunks and remove their actors.
    override fun unload() {
        log.info("Unloading ${loadedChunks.size} chunks")
        mutableSetOf<Chunk>().apply { addAll(loadedChunks) }.forEach { unloadChunk(it) }
    }

    override fun unloadChunk(chunk: Chunk, levelId: String) {
        loadedChunks.remove(chunk)
        super.unloadChunk(chunk, levelId)
    }

    override fun onRestore() {
        super.onRestore()
        populateChunks()
    }

    private fun xToChunkX(x: Int) = (x / CHUNK_SIZE) * CHUNK_SIZE + if (x < 0) -CHUNK_SIZE else 0
    private fun yToChunkY(y: Int) = (y / CHUNK_SIZE) * CHUNK_SIZE + if (y < 0) -CHUNK_SIZE else 0

    override fun chunkAt(x: Int, y: Int): Chunk? {
        if (x >= originChunkX && y >= originChunkY) {
            val cx = (x - originChunkX) shr 6 // OPTIMIZATION: this hardcodes CHUNK_SIZE to 64
            val cy = (y - originChunkY) shr 6
            if (cx >= 0 && cy >= 0 && cx < chunksWide && cy < chunksWide) {
                return chunks[cx][cy]
            }
        }
        return null
    }

    private fun populateChunks() {
        val chunkX = xToChunkX(pov.x)
        val chunkY = yToChunkY(pov.y)
        if (chunkX == lastPovChunkXY.x && chunkY == lastPovChunkXY.y) {  // player hasn't moved
            return
        }
        log.info("Entered chunk $chunkX $chunkY")
        lastPovChunkXY.x = chunkX
        lastPovChunkXY.y = chunkY
        originChunkX = chunkX - (CHUNK_SIZE * CHUNKS_AHEAD)
        originChunkY = chunkY - (CHUNK_SIZE * CHUNKS_AHEAD)

        val oldChunks = mutableSetOf<Chunk>().apply { addAll(loadedChunks) }
        for (cx in -CHUNKS_AHEAD..CHUNKS_AHEAD) {
            for (cy in -CHUNKS_AHEAD..CHUNKS_AHEAD) {
                getExistingChunkAt(chunkX + cx * CHUNK_SIZE, chunkY + cy * CHUNK_SIZE)?.also { existing ->
                    oldChunks.remove(existing)
                    chunks[cx + CHUNKS_AHEAD][cy + CHUNKS_AHEAD] = existing
                } ?: run {
                    loadChunkAt(chunkX + cx * CHUNK_SIZE, chunkY + cy * CHUNK_SIZE)
                }
            }
        }
        oldChunks.forEach { unloadChunk(it) }
        chunkAt(pov.x, pov.y)?.onPlayerEntered()
    }

    private fun getExistingChunkAt(x: Int, y: Int): Chunk? =
        loadedChunks.firstOrNull { it.x == x && it.y == y }

    private fun loadChunkAt(x: Int, y: Int) {
        ChunkLoader.getWorldChunkAt(this, x, y) { chunk ->
            receiveChunk(chunk)
        }
    }

    override fun receiveChunk(chunk: Chunk) {
        Metamap.markChunkMappedAt(chunk.x, chunk.y)
        val originCx = xToChunkX(pov.x) - CHUNK_SIZE * CHUNKS_AHEAD
        val originCy = yToChunkY(pov.y) - CHUNK_SIZE * CHUNKS_AHEAD
        val cx = (chunk.x - originCx) / CHUNK_SIZE
        val cy = (chunk.y - originCy) / CHUNK_SIZE
        if (cx < 0 || cy < 0 || cx >= chunksWide || cy >= chunksWide) {
            log.error("Received outdated chunk intended for cx $cx cy $cy !  Dropping")
            return
        }
        //log.info("Received new chunk $cx $cy (${loadedChunks.size} total)")
        val oldChunk = chunks[cx][cy]
        chunks[cx][cy] = chunk
        loadedChunks.add(chunk)
        chunk.onRestore(this)
        shadowDirty = true
        dirtyLightsAroundChunk(chunk)
        oldChunk?.also { if (!hasAttachedChunk(it)) unloadChunk(it) }
        if (playerChunk == null && xToChunkX(pov.x) == chunk.x && yToChunkY(pov.y) == chunk.y) {
            playerChunk = chunk
        }
        if (ambienceChunk == null && chunk.x == ambienceChunkXY.x && chunk.y == ambienceChunkXY.y) {
            transitionAmbienceToChunk(chunk)
        }
    }

    private fun hasAttachedChunk(chunk: Chunk): Boolean {
        var inUse = false
        for (x in 0 until chunksWide) {
            for (y in 0 until chunksWide) {
                if (chunks[x][y] == chunk) {
                    inUse = true
                }
            }
        }
        return inUse
    }

    private fun dirtyLightsAroundChunk(chunk: Chunk) {
        for (x in -1 .. chunk.width) {
            dirtyLightsTouching(x + chunk.x, chunk.y - 1)
            dirtyLightsTouching(x + chunk.x, chunk.y + chunk.height)
        }
        for (y in -1 .. chunk.height) {
            dirtyLightsTouching(chunk.x - 1, y + chunk.y)
            dirtyLightsTouching(chunk.x + chunk.width, y + chunk.y)
        }
    }

    private fun transitionAmbienceToChunk(chunk: Chunk) {
        if (chunk == ambienceChunk) return
        ambienceChunk = chunk
        updateAmbientSound()
    }

}
