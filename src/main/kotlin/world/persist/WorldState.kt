package world.persist

import actors.Player
import kotlinx.serialization.Serializable
import world.weather.Weather

@Serializable
data class WorldState(
    val levelId: String,
    val player: Player,
    val time: Double,
    val weather: Weather,
    val consoleLines: List<String>,
    val toolbarTags: List<String?>
)
