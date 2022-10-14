package things

import util.LightColor

interface LightSource {
    fun light(): LightColor?
}
