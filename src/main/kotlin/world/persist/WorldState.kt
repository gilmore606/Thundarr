package world.persist

import actors.Player
import kotlinx.serialization.Serializable
import things.Thing
import world.history.History
import world.weather.Weather

@Serializable
data class WorldState(
    val levelId: String,
    val player: Player,
    val time: Double,
    val weather: Weather,
    val history: History,
    val consoleLines: List<String>,
    val toolbarTags: List<Thing.Tag?>
)
