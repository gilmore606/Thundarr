package world.weather

import kotlinx.serialization.Serializable
import ui.panels.Console
import util.Dice
import util.LightColor
import world.Level
import java.lang.Float.max
import java.lang.Float.min
import kotlin.math.cos
import kotlin.math.sin

@Serializable
class Weather {

    var weatherIntensity = 0f
    var windX = 0f
    var windY = 0f
    var cloudIntensity = 0f
    var rainIntensity = 0f
    val lightning = LightColor(2f, 2f, 2f)

    var windSpeed = 0f
    var windDirection = 0.2f
    private val maxWindSpeed = 2f

    private var weatherIntensityTarget = 0f
    private var framesBeforeRaindrop = 0
    private var lastWeatherHour = 0

    fun clouds() = cloudIntensity
    fun rain() = rainIntensity

    fun shouldRaindrop(): Boolean {
        framesBeforeRaindrop--
        if (framesBeforeRaindrop < 0 && rainIntensity > 0.2f) {
            val rainInterval = (1800 - rainIntensity * 1600).toInt()
            framesBeforeRaindrop = rainInterval + Dice.oneTo(rainInterval)
            return true
        }
        return false
    }

    fun onRender(delta: Float) {
        weatherIntensity = if (weatherIntensity < weatherIntensityTarget) {
            java.lang.Float.min(weatherIntensityTarget, weatherIntensity + 0.25f * delta)
        } else {
            java.lang.Float.max(weatherIntensityTarget, weatherIntensity - 0.25f * delta)
        }

        var bolt = max(0f, lightning.r - 3.8f * delta)
        val boltChance = (rainIntensity - 0.4f) * 0.4f
        val extraChance = boltChance * 0.6f
        if (Dice.chance(delta * boltChance)) {
            bolt = 0.4f + Dice.float(0f, rainIntensity)
        }
        if (bolt > 0.01f) {
            if (Dice.chance(delta * extraChance)) {
                bolt += Dice.float(0.1f, 0.4f)
            }
        }
        lightning.r = bolt
        lightning.g = bolt
        lightning.b = bolt
    }

    fun updateTime(hour: Int, minute: Int, level: Level) {
        if (hour != lastWeatherHour) {
            lastWeatherHour = hour
            updateWeather(hour, minute, level)
        }
        val cloudLight = min(1f, (level.ambientLight.brightness() - 0.5f) * 2f)
        cloudIntensity = java.lang.Float.max(0f, min(1f, weatherIntensity * 2.0f) * cloudLight)
        rainIntensity = java.lang.Float.max(0f, (weatherIntensity - 0.5f) * 2f)
    }

    private fun updateWeather(hour: Int, minute: Int, level: Level) {
        if (Dice.chance(0.3f)) return
        var m = ""
        if (Dice.chance(0.5f)) {
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
        if (windSpeed < 0.2f && Dice.chance(0.5f)) {
            m += "The wind shifts.  "
            windDirection = (windDirection + Dice.float(0.2f, 0.8f)) % 1f
        }

        val windRads = (windDirection * 6.28318) - 3.14159
        windX = (cos(windRads) * windSpeed).toFloat()
        windY = (sin(windRads) * windSpeed).toFloat()

        if (Dice.chance(0.4f + weatherIntensity * 0.3f)) {
            // Rain more
            weatherIntensityTarget = min(1f, weatherIntensityTarget + 0.3f)
            if (!level.isRoofedAt(App.player.xy.x, App.player.xy.y)) {
                if (weatherIntensity < 0.45f && weatherIntensityTarget > 0.45f) {
                    m += "It begins to rain."
                } else if (weatherIntensity >= 0.5f) {
                    m += "The rain falls harder."
                } else if (hour in 7..18) {
                    m += "Clouds gather in the sky."
                }
            }
        } else {
            // Rain less
            weatherIntensityTarget = java.lang.Float.max(0f, weatherIntensityTarget - 0.3f)
            if (!level.isRoofedAt(App.player.xy.x, App.player.xy.y)) {
                if (weatherIntensity > 0.45f && weatherIntensityTarget < 0.45f) {
                    m += "It stops raining."
                } else if (weatherIntensity > 0.45f) {
                    m += "The rain lets up a bit."
                } else if (hour in 7..18) {
                    if (weatherIntensity > 0.3f) {
                        m += "The sun breaks through the clouds."
                    } else {
                        m += "The sun shines brightly."
                    }
                }
            }
        }
        if (m.isNotEmpty() && App.player.level == level) Console.say(m)
    }

}