package things

import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import util.LightColor

interface LightSource {
    fun light(): LightColor?
}
