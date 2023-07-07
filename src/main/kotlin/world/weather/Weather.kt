package world.weather

import actors.statuses.Status
import actors.statuses.Wet
import audio.Speaker
import kotlinx.serialization.Serializable
import ui.panels.Console
import util.Dice
import util.LightColor
import util.isEveryFrame
import world.Chunk
import world.level.Level
import java.lang.Float.max
import java.lang.Float.min
import kotlin.math.cos
import kotlin.math.sin

@Serializable
class Weather {

    val overallRaininess = 0.3f   // 0.4f

    var envString: String = "clear"

    var weatherIntensity = 0f
    var windX = 0f
    var windY = 0f
    var cloudIntensity = 0f
    var rainIntensity = 0f
    val lightning = LightColor(0f, 0f, 0f)

    var windSpeed = 0f
    var windDirection = 0.2f
    private val maxWindSpeed = 2f

    private var weatherIntensityTarget = 0f
    private var framesBeforeRaindrop = 0
    private var lastWeatherHour = 0

    fun clouds() = cloudIntensity
    fun rain() = rainIntensity

    fun temperature() = (-4f * windSpeed + -5f * rainIntensity + -5f * cloudIntensity).toInt()

    fun feltTemperature() = (-6f * windSpeed + -6f * rainIntensity).toInt()

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
            min(weatherIntensityTarget, weatherIntensity + 0.1f * delta)
        } else {
            max(weatherIntensityTarget, weatherIntensity - 0.1f * delta)
        }
        App.DEBUG_PERLIN?.also {
            weatherIntensity = 0f
            rainIntensity = 0f
        }

        var bolt = max(0f, lightning.r - 3.8f * delta)

        val boltChance = (rainIntensity - 0.4f) * 0.4f
        if (isEveryFrame(5)) {
            if (Dice.chance(delta * boltChance * 5f)) {
                bolt = 0.4f + Dice.float(0f, rainIntensity)
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
        if (hour != lastWeatherHour) {
            lastWeatherHour = hour
            updateWeather(hour, minute, level)
        }
        val cloudLight = min(1f, (level.ambientLight.brightness() - 0.5f) * 2f)
        cloudIntensity = max(0f, min(1f, weatherIntensity * 2.0f) * cloudLight)
        rainIntensity = max(0f, (weatherIntensity - 0.5f) * 2f)
        updateEnvString()
        updatePlayerWetness()
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

        if (Dice.chance(overallRaininess + weatherIntensity * 0.3f)) {
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
        if (m.isNotEmpty() && App.player.level == level &&
            level.roofedAt(App.player.xy.x, App.player.xy.y) != Chunk.Roofed.INDOOR)
            Console.say(m)
    }

    private fun updateEnvString() {
        envString = when {
            rainIntensity > 0.7f -> "stormy"
            rainIntensity > 0.3f -> "rainy"
            rainIntensity > 0.05f -> "drizzling"
            cloudIntensity > 0.65f -> "overcast"
            cloudIntensity > 0.3f -> "cloudy"
            else -> "clear"
        } + when {
            windSpeed > (maxWindSpeed * 0.8f) -> ", blustery"
            windSpeed > (maxWindSpeed * 0.6f) -> ", windy"
            windSpeed > (maxWindSpeed * 0.2f) -> ", breezy"
            else -> ", calm"
        }
    }

    private fun updatePlayerWetness() {
        if (rainIntensity > 0.1f) {
            if (App.player.level?.isRoofedAt(App.player.xy.x, App.player.xy.y) == false) {
                App.player.addWetness(rainIntensity * 0.2f, min(1f, rainIntensity * 2f))
            }
        }
    }

    fun forceWeather(intensity: Float) {
        weatherIntensity = intensity
        weatherIntensityTarget = intensity
        updateTime(App.gameTime.hour, App.gameTime.minute, App.level)
    }
}
