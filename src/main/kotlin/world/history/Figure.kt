package world.history

import kotlinx.serialization.Serializable
import util.Dice

@Serializable
class Figure(
    val id: Int
) {

    var name: String = ""

    var alive: Boolean = true
    var expectedLifespan = 55 + Dice.zeroTo(80)
    var birthYear: Int = App.history.year
    var deathYear: Int = 0

    fun passYears(yearsPassed: Int) {

    }
}
