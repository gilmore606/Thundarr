package world.weather

import audio.Speaker
import kotlinx.serialization.Serializable
import ui.panels.Console
import util.Dice
import util.LightColor
import util.isEveryFrame
import world.Chunk
import world.gen.Metamap
import world.level.Level
import world.level.WorldLevel
import java.lang.Math.max
import java.lang.Math.min
import kotlin.math.cos
import kotlin.math.sin

@Serializable
class Weather {

    enum class Type(
        val rank: Int,
        val displayName: String,
        val cloudVisual: Float,
        val rainVisual: Float,
        val snowVisual: Float,
        val temperatureMod: Float,
        val minTemp: Int,
        val maxTemp: Int,
        val upMsg: String,
        val downMsg: String,
        val doubleUpMsg: String,
        val doubleDownMsg: String,
    ) {
        CLEAR(0, "clear", 0f, 0f, 0f, 0f, -1000, 1000,
            "", "The clouds dissipate, leaving clear sky.",
            "", "The cloud cover breaks up and vanishes, leaving clear sky."),
        CLOUDY(1, "cloudy", 0.7f, 0f, 0f, -3f,-1000, 1000,
            "Light clouds gather in the sky.", "The cloud cover breaks up.",
            "", "The %rain suddenly stops, and the clouds break up."),
        OVERCAST(2, "overcast", 1f, 0f, 0f, -6f,-1000, 1000,
            "The clouds spread to blanket the sky.", "The %rain stops.",
            "Clouds roll quickly across the sky, blocking the sun.", "The storm abruptly stops."),
        RAINY(3, "rainy", 1f, 0.4f, 0f, -8f,25, 1000,
            "It begins to rain.", "The %rain lets up some.",
            "It begins to rain.", "The storm's fury suddenly calms."),
        SNOWY(3, "snowy", 0.5f, 0f, 0.5f, 0f, -1000, 35,
            "It starts snowing.", "It stops snowing.",
            "It starts snowing.", "It stops snowing."),
        STORMY(4, "stormy", 1f, 0.7f, 0f, -8f,25, 1000,
            "The rainfall grows to a downpour.", "The intense storm lessens its fury.",
            "The clouds burst with a sudden downpour.", ""),
        SNOWSTORM(4, "snowstorm", 1f, 0f, 1f, -5f, -1000, 30,
            "The snow falls harder.", "The blizzard lessens its fury.",
            "A heavy snowfall begins.", ""),
        MONSOON(5, "monsoon", 1f, 1f, 0f, -10f,35, 1000,
            "The storm intensifies!", "",
            "The clouds burst into an intense monsoon!", "")
        ;
        override fun toString() = displayName
    }

    var currentWeatherBias = -0.1f

    var type: Type = Type.CLEAR
    var cloudIntensity: Float = 0f
    var rainIntensity: Float = 0f
    var snowIntensity: Float = 0f

    var envString: String = "clear"

    var windX = 0f
    var windY = 0f
    val lightning = LightColor(0f, 0f, 0f)

    var windSpeed = 0f
    var windDirection = 0.2f
    private val maxWindSpeed = 2f
    private val fadeSpeed = 0.4f

    private var framesBeforeRaindrop = 0
    private var lastWeatherHour = 0

    fun clouds() = cloudIntensity
    fun rain() = rainIntensity
    fun snow() = snowIntensity

    fun temperature() = (-3f * windSpeed + type.temperatureMod).toInt()
    fun weatherTemperature() = (-6f * windSpeed) + (-5f * type.rainVisual)

    fun shouldRaindrop(): Boolean {
        framesBeforeRaindrop--
        if (framesBeforeRaindrop < 0 && type.rainVisual > 0f) {
            val rainInterval = (1800 - type.rainVisual * 1600).toInt()
            framesBeforeRaindrop = rainInterval + Dice.oneTo(rainInterval)
            return true
        }
        return false
    }

    fun onRender(delta: Float) {
        cloudIntensity = if (cloudIntensity < type.cloudVisual) {
            java.lang.Float.min(type.cloudVisual, cloudIntensity + fadeSpeed * delta)
        } else {
            java.lang.Float.max(type.cloudVisual, cloudIntensity - fadeSpeed * delta)
        }

        rainIntensity = if (rainIntensity < type.rainVisual) {
            java.lang.Float.min(type.rainVisual, rainIntensity + fadeSpeed * delta)
        } else {
            java.lang.Float.max(type.rainVisual, rainIntensity - fadeSpeed * delta)
        }

        snowIntensity = if (snowIntensity < type.snowVisual) {
            java.lang.Float.min(type.snowVisual, snowIntensity + fadeSpeed * delta)
        } else {
            java.lang.Float.max(type.snowVisual, snowIntensity - fadeSpeed * delta)
        }

        var bolt = java.lang.Float.max(0f, lightning.r - 3.8f * delta)

        val boltChance = (type.rainVisual - 0.4f) * 0.4f
        if (isEveryFrame(5)) {
            if (Dice.chance(delta * boltChance * 5f)) {
                bolt = 0.4f + Dice.float(0f, type.rainVisual)
                Speaker.world(if (bolt > 0.8f) Speaker.SFX.THUNDER_NEAR else Speaker.SFX.THUNDER_DISTANT,
                    delayMs = java.lang.Long.max(0L, 20L + (600f - Dice.float(0f, bolt / 0.0015f)).toLong()) )
            }
        }
        if (bolt > 0.01f) {
            val extraChance = boltChance * 0.6f
            if (Dice.chance(delta * extraChance)) {
                bolt += Dice.float(0.1f, 0.4f)
            }
        }
        lightning.r = bolt
        lightning.g = bolt
        lightning.b = bolt
    }

    fun flashLightning(color: LightColor) {
        lightning.r = color.r
        lightning.g = color.g
        lightning.b = color.b
        Speaker.world(Speaker.SFX.THUNDER_NEAR)
    }

    fun updateTime(hour: Int, minute: Int, level: Level) {
        updatePlayerWetness()
        if (hour != lastWeatherHour) {
            lastWeatherHour = hour
            update()
        }
    }

    fun update() {
        var m = ""
        if (Dice.chance(0.3f)) {
            // Wind speed change
            if (Dice.chance(0.7f - windSpeed * 0.5f)) {
                windSpeed += Dice.float(0.05f, 0.2f)
                m += "The wind picks up.  "
            } else if (Dice.chance(0.7f)) {
                windSpeed -= Dice.float(0.05f, 0.2f)
                m += "The wind dies down.  "
            }
        }
        windSpeed = min(windSpeed, maxWindSpeed)
        if (windSpeed < 0.2f && Dice.chance(0.3f)) {
            m += "The wind shifts.  "
            windDirection = (windDirection + Dice.float(0.2f, 0.8f)) % 1f
        }

        val windRads = (windDirection * 6.28318) - 3.14159
        windX = (cos(windRads) * windSpeed).toFloat()
        windY = (sin(windRads) * windSpeed).toFloat()

        // Pick a new weather
        if (App.player.level !is WorldLevel) return

        val temperature = App.player.level?.temperatureAt(App.player.xy) ?: 65
        val biome = Metamap.metaAtWorld(App.player.xy.x, App.player.xy.y).biome
        val maxRank = biome.maxWeatherRank() ?: Type.values().maxOf { it.rank }
        if (type.rank > maxRank) {
            changeWeather(getWeatherForRank(maxRank, temperature), m)
        } else if (type.rank == maxRank) {
            if (Dice.chance(0.5f)) {
                changeWeather(getWeatherForRank(maxRank - 1, temperature), m)
            }
        } else if (type.rank == 0) {
            if (Dice.chance(0.5f)) {
                changeWeather(getWeatherForRank(1, temperature), m)
            }
        } else {
            val roll = Dice.float(-1f, 2f) + biome.weatherBias() + currentWeatherBias
            var shift = when {
                roll < 0f -> -1
                roll in 0f..1f -> 0
                else -> 1
            }
            if (Dice.chance(0.1f)) shift *= 2
            val newRank = min(maxRank, max(0, type.rank + shift))
            changeWeather(getWeatherForRank(newRank, temperature), m)
        }

    }

    private fun getWeatherForRank(rank: Int, temperature: Int) = Type.values().filter {
        it.rank == rank && temperature >= it.minTemp && temperature <= it.maxTemp
    }.random()

    private fun changeWeather(newType: Type, partialMessage: String? = null) {
        if (newType == type) return

        val oldType = type
        type = newType
        val jump = type.rank - oldType.rank
        val tm = when {
            jump < -1 -> type.doubleDownMsg
            jump == -1 -> type.downMsg
            jump == 1 -> type.upMsg
            jump > 1 -> type.doubleUpMsg
            else -> ""
        }

        val m = ((partialMessage ?: "") + tm).replace("%rain", if (oldType.snowVisual > 0f) "snow" else "rain")
        if (m.isNotEmpty() &&
            App.player.level?.roofedAt(App.player.xy.x, App.player.xy.y) != Chunk.Roofed.INDOOR)
            Console.say(m)

        envString = type.displayName + ", " + windWord()
    }

    private fun windWord() = when {
        windSpeed > (maxWindSpeed * 0.8f) -> "blustery"
        windSpeed > (maxWindSpeed * 0.5f) -> "windy"
        windSpeed > (maxWindSpeed * 0.2f) -> "breezy"
        else -> "calm"
    }

    private fun updatePlayerWetness() {
        if (Dice.chance(0.6f)) return
        if (rainIntensity > 0.1f) {
            if (App.player.level?.isRoofedAt(App.player.xy.x, App.player.xy.y) == false) {
                App.player.addWetness(rainIntensity * 0.2f, min(1f, rainIntensity * 2f))
            }
        }
    }

    fun forceWeather(newType: Type) {
        changeWeather(newType)
    }
}
