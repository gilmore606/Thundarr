package util

import mu.KotlinLogging
import world.Chunk
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.UUID
import kotlin.math.pow
import kotlin.math.sqrt

inline fun <T> Iterable<T>.hasOneWhere(predicate: (T) -> Boolean): Boolean {
    for (element in this) if (predicate(element)) return true
    return false
}

inline fun <T> Iterable<T>.hasNoneWhere(predicate: (T) -> Boolean): Boolean {
    for (element in this) if (predicate(element)) return false
    return true
}

fun String.gzipCompress(): ByteArray {
    val bos = ByteArrayOutputStream()
    GZIPOutputStream(bos).bufferedWriter(Charsets.UTF_8).use { it.write(this) }
    return bos.toByteArray()
}

fun ByteArray.gzipDecompress(): String {
    val bais = ByteArrayInputStream(this)
    lateinit var string: String
    GZIPInputStream(bais).bufferedReader(Charsets.UTF_8).use { return it.readText() }
}

fun distanceBetween(x0: Int, y0: Int, x1: Int, y1: Int): Float =
    sqrt((x1-x0).toFloat().pow(2) + (y1-y0).toFloat().pow(2))

fun UUID() = UUID.randomUUID().toString()

fun String.aOrAn(): String {
    if (isNotEmpty()) {
        if (get(0) in listOf('a','e','i','o','u')) {
            return "an " + this
        }
    }
    return "a " + this
}

fun String.plural(): String {
    if (isNotEmpty()) {
        if (get(lastIndex) == 's') {
            return this + "es"
        }
    }
    return this + "s"
}

val log = KotlinLogging.logger {}

val NORTH = XY(0, -1)
val SOUTH = XY(0, 1)
val WEST = XY(-1, 0)
val EAST = XY(1, 0)

val NORTHEAST = XY(1, -1)
val NORTHWEST = XY(-1, -1)
val SOUTHEAST = XY(1, 1)
val SOUTHWEST = XY(-1, 1)

val NO_DIRECTION = XY(0, 0)

val CARDINALS = listOf(NORTH, SOUTH, WEST, EAST)
val CORNERS = listOf(NORTHEAST, NORTHWEST, SOUTHWEST, SOUTHEAST)
val DIRECTIONS = listOf(NORTH, SOUTH, WEST, EAST, NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST)
