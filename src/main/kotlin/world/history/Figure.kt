package world.history

import kotlinx.serialization.Serializable

@Serializable
class Figure(
    val id: Int
) {

    var name: String = ""

    var alive: Boolean = true
    var birthYear: Int = App.history.year
    var deathYear: Int = 0

}
