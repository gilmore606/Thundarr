package util

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import mu.KotlinLogging
import render.Screen
import things.Thing
import world.Entity
import world.journal.GameTime
import world.level.CHUNK_SIZE
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

fun <T> MutableList<T>.safeForEach(doThis: ((T)->Unit)) {
    var n = size
    while (n > 0) {
        n--
        doThis(this[n])
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

fun <T> MutableList<T>.total(iteration: (T)->Float): Float {
    var n = 0
    var t = 0f
    while (n < size) {
        t += iteration(this[n])
        n++
    }
    return t
}

fun MutableList<Thing>.groupByTag(): List<List<Thing>> {
    val groups = ArrayList<ArrayList<Thing>>()
    forEach {
        var found = false
        groups.forEach { group ->
            if (group[0].thingTag() == it.thingTag()) {
                group.add(it)
                found = true
            }
        }
        if (!found) groups.add(ArrayList<Thing>().apply { add(it) })
    }
    return groups
}

fun distanceBetween(x0: Int, y0: Int, x1: Int, y1: Int): Float =
    sqrt((x1-x0).toFloat().pow(2) + (y1-y0).toFloat().pow(2))
fun distanceBetween(xy0: XY, xy1: XY) = distanceBetween(xy0.x, xy0.y, xy1.x, xy1.y)

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

fun Float.turnsToRoughTime(): String {
    val turnsPerHour = GameTime.TURNS_PER_DAY / 24.0
    val hours = (this / turnsPerHour).toInt()
    if (hours < 1) return "under an hour"
    else if (hours == 1) return "an hour or so"
    else if (hours < 10) return "about " + hours.toEnglish() + " hours"
    else if (hours < 14) return "half a day"
    else if (hours < 21) return "better part of a day"
    else if (hours < 24) return "almost a day"
    else if (hours < 27) return "about a day"
    else if (hours < 30) return "more than a day"
    else if (hours < 39) return "a day and a half"
    else if (hours < 50) return "a couple days"
    else return (hours / 24).toEnglish() + " days"
}

fun Float.difficultyAdverb(skill: Float): String {
    val diff = this + 10f - skill
    return if (diff <= -5f) "almost certainly not"
    else if (diff <= -3f) "conceivably"
    else if (diff <= -1f) "possibly"
    else if (diff <= 1f) "likely"
    else if (diff <= 3f) "probably"
    else if (diff <= 5f) "almost certainly"
    else "trivially"
}

fun Float.difficultyDesc(skill: Float): String {
    val diff = this + 10f - skill
    return if (diff <= -6f) "impossible"
    else if (diff <= -4f) "very hard"
    else if (diff <= -2f) "hard"
    else if (diff <= 2f) "moderate"
    else if (diff <= 4f) "easy"
    else "trivial"
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
                "" -> "ten"
                "one" -> "eleven"
                "two" -> "twelve"
                "three" -> "thirteen"
                "five" -> "fifteen"
                else -> str + "teen"
            }
            2 -> "twenty" + if (str != "") "-"+str else ""
            3 -> "thirty" + if (str != "") "-"+str else ""
            4 -> "forty" + if (str != "") "-"+str else ""
            5 -> "fifty" + if (str != "") "-"+str else ""
            6 -> "sixty" + if (str != "") "-"+str else ""
            7 -> "seventy" + if (str != "") "-"+str else ""
            8 -> "eighty" + if (str != "") "-"+str else ""
            9 -> "ninety" + if (str != "") "-"+str else ""
            else -> "???" + str
        }
    }
    return str
}

fun Double.toGameTimeString() {

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

fun List<String>.toEnglishList(articles: Boolean = true): String {
    val grouped = this.groupBy { it }.toList()
    var str = ""
    grouped.forEachIndexed { n, s ->
        str += if (s.second.size <= 1) (if (articles) s.first.aOrAn() else s.first) else s.second.size.toEnglish() + " " + s.first.plural()
        str += if (n == grouped.lastIndex) "" else if (n == grouped.lastIndex - 1) " and " else ", "
    }
    return str
}

fun <T>MutableSet<Pair<T,Float>>.addIfHeavier(item: T, weight: Float) {
    removeIf { it.first == item && it.second < weight }
    add(Pair(item, weight))
}

fun isEveryFrame(frames: Int): Boolean = (Screen.timeMs % frames.toLong() == 0L)

fun groundAtPlayer() = App.level.cellContainerAt(App.player.xy.x, App.player.xy.y)

fun wrapText(text: String, width: Int, padding: Int, font: BitmapFont = Screen.smallFont): ArrayList<String> {
    val wrapped = ArrayList<String>()
    var remaining = text
    var nextLine = ""
    var linePixelsLeft = (width - padding * 2)
    val spaceWidth = GlyphLayout(font, " ").width.toInt()
    while (remaining.isNotEmpty() || remaining == " ") {
        // get next word
        val space = remaining.indexOf(' ')
        var word = ""
        if (space >= 0) {
            word = remaining.substring(0, space)
            remaining = remaining.substring(space + 1, remaining.length)
        } else {
            word = remaining
            remaining = ""
        }
        if (word != " " && word != "") {
            val wordWidth = GlyphLayout(font, word).width.toInt()
            if (nextLine == "" || wordWidth <= linePixelsLeft) {
                nextLine += word + " "
                linePixelsLeft -= wordWidth + spaceWidth
            } else {
                wrapped.add(nextLine)
                nextLine = word + " "
                linePixelsLeft = (width - padding * 2) - wordWidth - spaceWidth
            }
        }
    }
    if (nextLine != "") wrapped.add(nextLine)
    return wrapped
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
val DIAGONALS = listOf(NORTHEAST, NORTHWEST, SOUTHWEST, SOUTHEAST)
val DIRECTIONS = listOf(NORTH, SOUTH, WEST, EAST, NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST)

fun dirToEdge(dir: XY, offset: Int = 0): XY = when (dir) {
    NORTH -> XY(CHUNK_SIZE / 2 + offset, 0)
    SOUTH -> XY(CHUNK_SIZE / 2 + offset, CHUNK_SIZE - 1)
    WEST -> XY(0, CHUNK_SIZE / 2 + offset)
    EAST -> XY(CHUNK_SIZE - 1, CHUNK_SIZE / 2 + offset)
    NORTHEAST -> XY(CHUNK_SIZE - 1, 0)
    SOUTHEAST -> XY(CHUNK_SIZE - 1, CHUNK_SIZE - 1)
    NORTHWEST -> XY(0, 0)
    SOUTHWEST -> XY(0, CHUNK_SIZE - 1)
    else -> XY(0,0)
}

fun getBezier(t: Float, start: XYf, startControl: XYf, endControl: XYf, end: XYf): XYf {
    val u = 1 - t
    val tt = t*t
    val uu = u*u
    val uuu = uu*u
    val ttt = tt*t
    var p = start * uuu
    p += startControl * 3f * uu * t
    p += endControl * 3f * u * tt
    p += end * ttt
    return p
}

fun randomChunkEdgePos(edge: XY, variance: Float) = when (edge) {
    NORTH -> XY(CHUNK_SIZE/2 + (Dice.float(-CHUNK_SIZE/2f,CHUNK_SIZE/2f)*variance).toInt(), 0)
    SOUTH -> XY(CHUNK_SIZE/2 + (Dice.float(-CHUNK_SIZE/2f,CHUNK_SIZE/2f)*variance).toInt(), CHUNK_SIZE - 1)
    WEST -> XY(0, CHUNK_SIZE/2 +(Dice.float(-CHUNK_SIZE/2f,CHUNK_SIZE/2f)*variance).toInt())
    else -> XY(CHUNK_SIZE - 1, CHUNK_SIZE/2 + (Dice.float(-CHUNK_SIZE/2f,CHUNK_SIZE/2f)*variance).toInt())
}

fun flipChunkEdgePos(edge: XY) = when {
    edge.x == 0 -> XY(CHUNK_SIZE-1, edge.y)
    edge.x == CHUNK_SIZE-1 -> XY(0, edge.y)
    edge.y == 0 -> XY(edge.x, CHUNK_SIZE-1)
    else -> XY(edge.x, 0)
}
