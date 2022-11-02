package util

import mu.KotlinLogging
import world.Chunk
import world.Entity
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.UUID
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

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

fun <T> MutableList<T>.filterOut(condition: (T)->Boolean, elseDo: ((T)->Unit)? = null) {
    var n = 0
    while (n < size) {
        if (condition(this[n])) {
            removeAt(n)
            n--
        } else elseDo?.invoke(this[n])
        n++
    }
}

fun <T> MutableList<T>.filterAnd(condition: (T)->Boolean, thenDo: ((T)->Unit)? = null) {
    var n = 0
    while (n < size) {
        if (condition(this[n])) {
            thenDo?.invoke(removeAt(n)) ?: run { removeAt(n) }
            n--
        }
        n++
    }
}

fun <T> MutableList<T>.iterateAndEmpty(iteration: (T)->Unit) {
    while (isNotEmpty()) {
        val first = first()
        iteration(first)
        remove(first)
    }
}

fun distanceBetween(x0: Int, y0: Int, x1: Int, y1: Int): Float =
    sqrt((x1-x0).toFloat().pow(2) + (y1-y0).toFloat().pow(2))

fun UUID() = UUID.randomUUID().toString()
fun shortID() = Random.nextInt(100000000).toString()

fun String.aOrAn(): String {
    if (isNotEmpty()) {
        if (get(0) in listOf('a','e','i','o','u')) {
            return "an " + this
        }
    }
    return "a " + this
}

fun Int.toEnglish(): String {
    if (this > 99) return this.toString()
    var str =  when (this % 10) {
        0 -> if (this < 10) "zero" else ""
        1 -> "one"
        2 -> "two"
        3 -> "three"
        4 -> "four"
        5 -> "five"
        6 -> "six"
        7 -> "seven"
        8 -> "eight"
        9 -> "nine"
        else -> "???"
    }
    if (this > 9) {
        str = when ((this / 10) % 10) {
            0 -> str
            1 -> when (str) {
                "one" -> "eleven"
                "two" -> "twelve"
                "three" -> "thirteen"
                "five" -> "fifteen"
                else -> str + "teen"
            }
            2 -> "twenty-" + str
            3 -> "thirty-" + str
            4 -> "forty-" + str
            5 -> "fifty-" + str
            6 -> "sixty-" + str
            7 -> "seventy-" + str
            8 -> "eighty-" + str
            9 -> "ninety-" + str
            else -> "???" + str
        }
    }
    return str
}

fun String.plural(): String {
    if (isNotEmpty()) {
        if (listOf(
                "s", "ch", "sh"
            ).hasOneWhere { this.endsWith(it) }) {
            return this + "es"
        }
    }
    return this + "s"
}

fun List<Entity>.englishList(): String {
    val grouped = this.groupBy { it.name() }.toList()
    var str = ""
    grouped.forEachIndexed { n, s ->
        str += if (s.second.size <= 1) s.first.aOrAn() else s.second.size.toEnglish() + " " + s.first.plural()
        str += if (n == grouped.lastIndex) "" else if (n == grouped.lastIndex - 1) " and " else ", "
    }
    return str
}

fun groundAtPlayer() = App.level.cellContainerAt(App.player.xy.x, App.player.xy.y)

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
